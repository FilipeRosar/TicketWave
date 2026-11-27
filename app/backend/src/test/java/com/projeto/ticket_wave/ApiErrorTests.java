package com.projeto.ticket_wave;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.CategoryJpaRepository;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.VenueJpaRepository;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.AddressJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.CategoryJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.VenueJpaEntity;
import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtService;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.GoogleIdentityVerifier;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaEntity;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ApiErrorTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactions;

    @MockitoBean
    private JavaMailSender mail;

    @MockitoBean
    private GoogleIdentityVerifier google;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwords;

    @Autowired
    private CategoryJpaRepository categories;

    @Autowired
    private VenueJpaRepository venues;

    @Autowired
    private OrganizationJpaRepository organizations;

    @MockitoSpyBean
    private OrganizationMemberJpaRepository organizationMembers;

    @DynamicPropertySource
    static void organizationDocumentKey(DynamicPropertyRegistry registry) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        String encodedKey = Base64.getEncoder().encodeToString(key);
        registry.add("app.organizations.document-key", () -> encodedKey);
    }

    @Test
    void registersLogsInRotatesAndRevokesTokens() throws Exception {
        String auth = "/v1/api/auth";
        String protectedPath = auth + "/me";
        expectError(mvc.perform(get(protectedPath)), 401, protectedPath);
        expectError(mvc.perform(get(protectedPath).header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")),
                401, protectedPath).andExpect(jsonPath("$.message").value("Token inválido"));
        mvc.perform(post(auth + "/register").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"JWT Test","email":"jwt-flow@example.test","password":"test-password","phone":"11944445555",
                         "birthDate":"1995-04-20","cpf":"529.982.247-25","passwordConfirmation":"test-password",
                         "address":{"zipCode":"01001-000","street":"Praça da Sé","number":"S/N","district":"Sé","city":"São Paulo","state":"SP"}}
                        """))
                .andExpect(status().isOk());

        User registered = users.findByEmail("jwt-flow@example.test").orElseThrow();
        assertThat(registered.getBirthDate()).isEqualTo(LocalDate.of(1995, 4, 20));
        assertThat(registered.getCpf()).isEqualTo("52998224725");
        assertThat(registered.getRegistrationAddress().zipCode()).isEqualTo("01001000");
        assertThat(registered.getRegistrationAddress().street()).isEqualTo("Praça da Sé");
        assertThat(registered.getRegistrationAddress().number()).isEqualTo("S/N");

        String login = """
                {"email":"jwt-flow@example.test","password":"test-password"}
                """;
        expectError(mvc.perform(post(auth + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(login.replace("test-password", "wrong-password"))), 401, auth + "/login");
        String loginBody = mvc.perform(post(auth + "/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        var session = json.readTree(loginBody);
        mvc.perform(get(protectedPath).header(HttpHeaders.AUTHORIZATION, "Bearer " + session.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jwt-flow@example.test"));

        String oldRefresh = json.createObjectNode().put("refreshToken", session.get("refreshToken").asText()).toString();
        String refreshedBody = mvc.perform(post(auth + "/refresh")
                .contentType(MediaType.APPLICATION_JSON).content(oldRefresh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        var refreshed = json.readTree(refreshedBody);
        mvc.perform(get(protectedPath).header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshed.get("accessToken").asText()))
                .andExpect(status().isOk());
        expectError(mvc.perform(post(auth + "/refresh").contentType(MediaType.APPLICATION_JSON).content(oldRefresh)),
                401, auth + "/refresh");

        String currentRefresh = json.createObjectNode().put("refreshToken", refreshed.get("refreshToken").asText()).toString();
        mvc.perform(post(auth + "/logout").contentType(MediaType.APPLICATION_JSON).content(currentRefresh))
                .andExpect(status().isOk());
        expectError(mvc.perform(post(auth + "/refresh").contentType(MediaType.APPLICATION_JSON).content(currentRefresh)),
                401, auth + "/refresh");
    }

    @Test
    void formatsValidationAndMalformedRequests() throws Exception {
        String path = "/v1/api/auth/register";
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}")), 400, path)
                .andExpect(jsonPath("$.violations.length()").value(8));
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{")), 400, path)
                .andExpect(jsonPath("$.violations").isEmpty());
        expectError(mvc.perform(post(path).contentType(MediaType.TEXT_PLAIN).content("{}")), 415, path);
        expectError(mvc.perform(get(path)), 405, path)
                .andExpect(header().string(HttpHeaders.ALLOW, "POST"));
    }

    @Test
    void formatsAuthenticationFailures() throws Exception {
        String path = "/v1/api/auth/refresh";
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"unknown-token\"}")), 401, path);
        expectError(mvc.perform(get("/users/me")), 401, "/users/me");
    }

    @Test
    void formatsDuplicateRegistration() throws Exception {
        String path = "/v1/api/auth/register";
        String registration = """
                {"name":"Nova conta","email":"duplicate@example.test","password":"test-password","phone":"11933334444",
                 "birthDate":"1995-04-20","cpf":"111.444.777-35","passwordConfirmation":"test-password",
                         "address":{"zipCode":"01001-000","street":"Praça da Sé","number":"S/N","district":"Sé","city":"São Paulo","state":"SP"}}
                """;
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("111.444.777-35", "111.111.111-11"))), 400, path)
                .andExpect(jsonPath("$.violations[0].field").value("cpf"));
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("01001-000", "123"))), 400, path)
                .andExpect(jsonPath("$.violations[0].field").value("address.zipCode"));
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("1995-04-20", "2099-01-01"))), 400, path)
                .andExpect(jsonPath("$.violations[0].field").value("birthDate"));
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("\"passwordConfirmation\":\"test-password\"", "\"passwordConfirmation\":\"wrong-password\""))),
                400, path).andExpect(jsonPath("$.violations[0].message").value("As senhas devem ser iguais."));
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isOk());
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(registration)), 409, path);
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("duplicate@example.test", "another@example.test")
                        .replace("111.444.777-35", "12345678909"))), 409, path);
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("duplicate@example.test", "another@example.test")
                        .replace("11933334444", "11933336666").replace("111.444.777-35", "11144477735"))), 409, path);
    }

    @Test
    void formatsContainerErrorsWithoutExposingExceptionDetails() throws Exception {
        String originalPath = "/v1/api/event";
        expectError(mvc.perform(get("/error").with(request -> {
            request.setDispatcherType(DispatcherType.ERROR);
            return request;
        }).requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, originalPath)
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Internal exception details")),
                500, originalPath)
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno. Tente novamente mais tarde."));
    }

    @Test
    @Transactional
    void formatsAuthorizationLookupAndEventValidation() throws Exception {
        User user = new User();
        user.setName("Test user");
        user.setEmail("api-errors@example.test");
        user.setPassword("test-hash");
        user.setPhone("11911112222");
        user.setRole(Role.ROLE_CUSTOMER);
        user = users.save(user);
        String customerToken = "Bearer " + jwtService.generateToken(new CustomUserDetails(user));

        String eventPath = "/v1/api/event";
        String missingUser = "/users/" + UUID.randomUUID();
        expectError(mvc.perform(get(missingUser).header(HttpHeaders.AUTHORIZATION, customerToken)), 404, missingUser);
        expectError(mvc.perform(get("/users/invalid-id").header(HttpHeaders.AUTHORIZATION, customerToken)), 400, "/users/invalid-id");
        expectError(mvc.perform(get("/nonexistent").header(HttpHeaders.AUTHORIZATION, customerToken)), 404, "/nonexistent");

        String event = """
                {
                  "title": "Evento de teste",
                  "shortDescription": "Descrição curta",
                  "description": "Descrição do evento",
                  "startDate": "2099-01-01T10:00:00",
                  "endDate": "2099-01-01T11:00:00",
                  "capacity": 100,
                  "ticketPrice": 50.00,
                  "eventType": "ONLINE",
                  "categoryId": "00000000-0000-0000-0000-000000000001",
                  "venueId": "00000000-0000-0000-0000-000000000002"
                }
                """;
        expectError(mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, customerToken)
                .contentType(MediaType.APPLICATION_JSON).content(event)), 403, eventPath);

        expectError(mvc.perform(post(eventPath).contentType(MediaType.APPLICATION_JSON).content(event)), 401, eventPath);

        String adminToken = bearerFor("admin-role@example.test", "11999991111", Role.ROLE_ADMIN);
        expectError(mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(event)), 403, eventPath);

        user.setRole(Role.ROLE_ORGANIZER);
        User organizer = users.save(user);
        String organizerToken = "Bearer " + jwtService.generateToken(new CustomUserDetails(organizer));
        expectError(mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content(event.replace("\"capacity\": 100", "\"capacity\": 0"))),
                400, eventPath).andExpect(jsonPath("$.violations[0].field").value("capacity"));

        CategoryJpaEntity category = new CategoryJpaEntity();
        category.setName("Autorização");
        category.setSlug("autorizacao");
        AddressJpaEntity address = new AddressJpaEntity();
        address.setStreet("Rua de teste");
        address.setNumber("1");
        address.setDistrict("Centro");
        address.setCity("São Paulo");
        address.setState("SP");
        address.setZipCode("01000-000");
        VenueJpaEntity venue = new VenueJpaEntity();
        venue.setName("Auditório de teste");
        venue.setSlug("auditorio-de-teste");
        venue.setCapacity(100);
        venue.setAddress(address);
        String validEvent = event.replace("00000000-0000-0000-0000-000000000001", categories.save(category).getId().toString())
                .replace("00000000-0000-0000-0000-000000000002", venues.save(venue).getId().toString());
        expectError(mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validEvent.replace("2099-01-01T11:00:00", "2099-01-01T09:00:00"))), 400, eventPath);

        String created = mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content(validEvent))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventStatus").value("DRAFT"))
                .andExpect(jsonPath("$.published").value(false)).andReturn().getResponse().getContentAsString();
        String id = json.readTree(created).get("id").asText();
        String itemPath = eventPath + "/" + id;
        String publishPath = itemPath + "/publish";
        String cancelPath = itemPath + "/cancel";
        String minePath = eventPath + "/mine";
        String searchPath = eventPath + "/search";

        String participantToken = bearerFor("participant-role@example.test", "11999992222", Role.ROLE_CUSTOMER);
        String otherOrganizerToken = bearerFor("organizer-role@example.test", "11999993333", Role.ROLE_ORGANIZER);
        expectError(mvc.perform(get(itemPath).header(HttpHeaders.AUTHORIZATION, participantToken)), 404, itemPath);
        expectError(mvc.perform(get(minePath).header(HttpHeaders.AUTHORIZATION, participantToken)), 403, minePath);
        mvc.perform(get(minePath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(get(minePath).header(HttpHeaders.AUTHORIZATION, otherOrganizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(get(searchPath).header(HttpHeaders.AUTHORIZATION, participantToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());

        expectError(mvc.perform(put(itemPath).contentType(MediaType.APPLICATION_JSON).content("{}")), 401, itemPath);
        expectError(mvc.perform(delete(itemPath)), 401, itemPath);
        expectError(mvc.perform(patch(publishPath)), 401, publishPath);
        expectError(mvc.perform(patch(cancelPath)), 401, cancelPath);
        for (String deniedToken : new String[]{participantToken, adminToken, otherOrganizerToken}) {
            expectError(mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, deniedToken)
                    .contentType(MediaType.APPLICATION_JSON).content("{}")), 403, itemPath);
            expectError(mvc.perform(delete(itemPath).header(HttpHeaders.AUTHORIZATION, deniedToken)), 403, itemPath);
            expectError(mvc.perform(patch(publishPath).header(HttpHeaders.AUTHORIZATION, deniedToken)), 403, publishPath);
            expectError(mvc.perform(patch(cancelPath).header(HttpHeaders.AUTHORIZATION, deniedToken)), 403, cancelPath);
        }

        expectError(mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"startDate":"2099-01-01T12:00:00"}
                        """)), 400, itemPath);
        LocalDateTime now = LocalDateTime.now();
        entityManager.createQuery("""
                UPDATE Event e SET e.startDate = :start, e.availableTickets = 80 WHERE e.id = :id
                """).setParameter("start", now.minusDays(1)).setParameter("id", UUID.fromString(id)).executeUpdate();
        entityManager.clear();
        expectError(mvc.perform(patch(publishPath).header(HttpHeaders.AUTHORIZATION, organizerToken)), 400, publishPath);
        expectError(mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"capacity":19}
                        """)), 400, itemPath);
        mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"Evento atualizado","startDate":"2099-01-01T10:00:00","capacity":60}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Evento atualizado"))
                .andExpect(jsonPath("$.availableTickets").value(40));

        mvc.perform(patch(publishPath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.published").value(true));
        mvc.perform(patch(publishPath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.published").value(true));
        mvc.perform(get(itemPath).header(HttpHeaders.AUTHORIZATION, participantToken))
                .andExpect(status().isOk());
        mvc.perform(get(searchPath).header(HttpHeaders.AUTHORIZATION, participantToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(id));
        mvc.perform(get(itemPath).header(HttpHeaders.AUTHORIZATION, participantToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("Autorização"))
                .andExpect(jsonPath("$.classification").value("SOCIAL"))
                .andExpect(jsonPath("$.networkingFeatures[0]").value("WALL"))
                .andExpect(jsonPath("$.networkingFeatures[1]").value("GROUPS"))
                .andExpect(jsonPath("$.networkingFeatures.length()").value(2));
        expectError(mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"classification":"UNKNOWN"}
                        """)), 400, itemPath);
        mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"classification":"PROFESSIONAL"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventType").value("ONLINE"))
                .andExpect(jsonPath("$.classification").value("PROFESSIONAL"));
        mvc.perform(get(itemPath).header(HttpHeaders.AUTHORIZATION, participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.networkingFeatures[0]").value("MEETING_AGENDA"))
                .andExpect(jsonPath("$.networkingFeatures[1]").value("ROOMS"))
                .andExpect(jsonPath("$.networkingFeatures.length()").value(2));
        mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"networkingEnabled":false}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.classification").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.networkingFeatures").isEmpty());
        expectError(mvc.perform(delete(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)), 409, itemPath);

        mvc.perform(patch(cancelPath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.published").value(false));
        mvc.perform(patch(cancelPath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventStatus").value("CANCELLED"));
        mvc.perform(get(minePath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].status").value("CANCELLED"));
        mvc.perform(get(searchPath).header(HttpHeaders.AUTHORIZATION, participantToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        expectError(mvc.perform(get(itemPath).header(HttpHeaders.AUTHORIZATION, participantToken)), 404, itemPath);
        expectError(mvc.perform(patch(publishPath).header(HttpHeaders.AUTHORIZATION, organizerToken)), 409, publishPath);
        expectError(mvc.perform(put(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content("{}")), 409, itemPath);
        expectError(mvc.perform(delete(itemPath).header(HttpHeaders.AUTHORIZATION, organizerToken)), 409, itemPath);

        String secondDraft = mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content(json.readValue(validEvent, ObjectNode.class)
                        .put("title", "Rascunho para excluir").put("classification", "PROFESSIONAL").toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.classification").value("PROFESSIONAL"))
                .andReturn().getResponse().getContentAsString();
        String draftPath = eventPath + "/" + json.readTree(secondDraft).get("id").asText();
        mvc.perform(delete(draftPath).header(HttpHeaders.AUTHORIZATION, organizerToken))
                .andExpect(status().isNoContent());
        expectError(mvc.perform(get(draftPath).header(HttpHeaders.AUTHORIZATION, organizerToken)), 404, draftPath);
    }

    @Test
    @Transactional
    void participantCanCreateViewEditAndDeleteTheirOwnProfile() throws Exception {
        String path = "/v1/api/profile/me";
        String owner = bearerFor("profile@example.test", "11999994444", Role.ROLE_CUSTOMER);
        String other = bearerFor("other-profile@example.test", "11999995555", Role.ROLE_CUSTOMER);
        String organizer = bearerFor("profile-organizer@example.test", "11999996666", Role.ROLE_ORGANIZER);

        expectError(mvc.perform(get(path)), 401, path);
        expectError(mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, organizer)), 403, path);
        expectError(mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, owner)), 404, path);
        expectError(mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, owner)
                .param("name", " ")), 400, path);
        expectError(mvc.perform(multipart(HttpMethod.PUT, path)
                .file(new MockMultipartFile("photo", "invalid.png", "image/png", new byte[]{1, 2, 3}))
                .header(HttpHeaders.AUTHORIZATION, owner).param("name", "Nome do perfil")), 400, path);

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", png);
        String encodedPhoto = Base64.getEncoder().encodeToString(png.toByteArray());
        mvc.perform(multipart(HttpMethod.PUT, path)
                .file(new MockMultipartFile("photo", "profile.png", "image/png", png.toByteArray()))
                .header(HttpHeaders.AUTHORIZATION, owner)
                .param("name", "Nome do perfil").param("occupation", "Designer")
                .param("city", "São Paulo").param("bio", "Acompanho eventos de design."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoType").value("image/png"))
                .andExpect(jsonPath("$.photo").value(encodedPhoto));
        mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.occupation").value("Designer"))
                .andExpect(jsonPath("$.city").value("São Paulo"))
                .andExpect(jsonPath("$.bio").value("Acompanho eventos de design."));
        expectError(mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, other)), 404, path);

        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, owner)
                .param("name", "Nome atualizado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome atualizado"))
                .andExpect(jsonPath("$.photo").value(encodedPhoto));
        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, owner)
                .param("name", "Nome atualizado").param("removePhoto", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.photo").isEmpty());

        mvc.perform(delete(path).header(HttpHeaders.AUTHORIZATION, owner)).andExpect(status().isNoContent());
        expectError(mvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, owner)), 404, path);
        mvc.perform(get("/v1/api/auth/me").header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("profile@example.test"));
    }

    @Test
    @Transactional
    void suggestsConnectionsBySharedInterestsAndCompatibleGoals() throws Exception {
        String path = "/v1/api/profile/me";
        String viewer = bearerFor("suggestions@example.test", "11999994101", Role.ROLE_CUSTOMER);
        String bestMatch = bearerFor("best-match@example.test", "11999994102", Role.ROLE_CUSTOMER);
        String goalMatch = bearerFor("goal-match@example.test", "11999994103", Role.ROLE_CUSTOMER);
        String noMatch = bearerFor("no-match@example.test", "11999994104", Role.ROLE_CUSTOMER);
        String suggestionsPath = path + "/suggestions";
        expectError(mvc.perform(get(suggestionsPath)), 401, suggestionsPath);

        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, viewer)
                .param("name", "Meu perfil").param("interests", " Design ", "design", "Música")
                .param("goals", "HIRE", "PARTNERSHIP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interests[0]").value("design"))
                .andExpect(jsonPath("$.interests[1]").value("música"))
                .andExpect(jsonPath("$.interests.length()").value(2));
        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, bestMatch)
                .param("name", "Ana").param("interests", "DESIGN", "música")
                .param("goals", "GET_HIRED"))
                .andExpect(status().isOk());
        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, goalMatch)
                .param("name", "Bruna").param("goals", "PARTNERSHIP"))
                .andExpect(status().isOk());
        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, noMatch)
                .param("name", "Caio").param("goals", "HIRE"))
                .andExpect(status().isOk());
        mvc.perform(get(suggestionsPath).header(HttpHeaders.AUTHORIZATION, viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ana"))
                .andExpect(jsonPath("$[0].sharedInterests.length()").value(2))
                .andExpect(jsonPath("$[0].matchedGoals[0]").value("GET_HIRED"))
                .andExpect(jsonPath("$[1].name").value("Bruna"))
                .andExpect(jsonPath("$[1].matchedGoals[0]").value("PARTNERSHIP"));

        mvc.perform(multipart(HttpMethod.PUT, path).header(HttpHeaders.AUTHORIZATION, viewer)
                .param("name", "Meu perfil"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.goals").isEmpty());
        mvc.perform(get(suggestionsPath).header(HttpHeaders.AUTHORIZATION, viewer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void passwordResetEmailExpiresAndTokenCanOnlyBeConsumedOnce() throws Exception {
        String auth = "/v1/api/auth";
        String email = "password-reset@example.test";
        String registration = """
                {"name":"Pessoa Teste","email":"password-reset@example.test","password":"old-password","phone":"11999997210",
                 "birthDate":"1995-04-20","cpf":"153.509.460-56","passwordConfirmation":"old-password",
                 "address":{"zipCode":"01001-000","street":"Praça da Sé","number":"S/N","district":"Sé","city":"São Paulo","state":"SP"}}
                """;
        mvc.perform(post(auth + "/register").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isOk());
        String originalLogin = json.createObjectNode().put("email", email).put("password", "old-password").toString();
        String loginBody = mvc.perform(post(auth + "/login").contentType(MediaType.APPLICATION_JSON).content(originalLogin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String refresh = json.createObjectNode().put("refreshToken", json.readTree(loginBody).get("refreshToken").asText()).toString();

        String requestPath = auth + "/forgot-password";
        String resetPath = auth + "/reset-password";
        String recovery = json.createObjectNode().put("email", email).toString();
        mvc.perform(post(requestPath).contentType(MediaType.APPLICATION_JSON)
                .content(recovery.replace(email, "unknown@example.test")))
                .andExpect(status().isAccepted()).andExpect(content().string(""));
        mvc.perform(post(requestPath).contentType(MediaType.APPLICATION_JSON).content(recovery))
                .andExpect(status().isAccepted()).andExpect(content().string(""));

        var messages = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, timeout(5000)).send(messages.capture());
        assertThat(messages.getValue().getTo()).containsExactly(email);
        String expired = resetTokenFrom(messages.getValue());
        assertThat(expired).matches("[A-Za-z0-9_-]{43}");
        UUID userId = users.findByEmail(email).orElseThrow().getId();
        transactions.executeWithoutResult(transaction -> {
            String hash = entityManager.createQuery("""
                    SELECT token.tokenHash FROM PasswordResetTokenJpaEntity token WHERE token.userId = :id
                    """, String.class).setParameter("id", userId).getSingleResult();
            assertThat(hash).hasSize(64).isNotEqualTo(expired);
            entityManager.createQuery("""
                    UPDATE PasswordResetTokenJpaEntity token SET token.expiresAt = :expiredAt WHERE token.userId = :id
                    """).setParameter("expiredAt", Instant.now().minusSeconds(1)).setParameter("id", userId).executeUpdate();
        });
        expectError(mvc.perform(post(resetPath).contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(expired, "new-password"))), 400, resetPath);

        mvc.perform(post(requestPath).contentType(MediaType.APPLICATION_JSON).content(recovery))
                .andExpect(status().isAccepted());
        var delivered = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, timeout(5000).times(2)).send(delivered.capture());
        String valid = resetTokenFrom(delivered.getValue());
        assertThat(valid).isNotEqualTo(expired);
        expectError(mvc.perform(post(resetPath).contentType(MediaType.APPLICATION_JSON)
                .content(json.createObjectNode().put("token", valid).put("password", "new-password")
                        .put("passwordConfirmation", "wrong-password").toString())), 400, resetPath);

        String reset = resetBody(valid, "new-password");
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> mvc.perform(post(resetPath).contentType(MediaType.APPLICATION_JSON).content(reset))
                    .andReturn().getResponse().getStatus());
            var second = executor.submit(() -> mvc.perform(post(resetPath).contentType(MediaType.APPLICATION_JSON).content(reset))
                    .andReturn().getResponse().getStatus());
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(204, 400);
        }
        expectError(mvc.perform(post(auth + "/login").contentType(MediaType.APPLICATION_JSON).content(originalLogin)),
                401, auth + "/login");
        expectError(mvc.perform(post(auth + "/refresh").contentType(MediaType.APPLICATION_JSON).content(refresh)),
                401, auth + "/refresh");
        mvc.perform(post(auth + "/login").contentType(MediaType.APPLICATION_JSON)
                .content(originalLogin.replace("old-password", "new-password")))
                .andExpect(status().isOk());
    }

    private String resetTokenFrom(SimpleMailMessage message) {
        return message.getText().lines()
                .filter(line -> line.contains("/redefinir-senha#")).findFirst().orElseThrow()
                .split("#", 2)[1];
    }

    private String resetBody(String token, String password) {
        return json.createObjectNode().put("token", token).put("password", password)
                .put("passwordConfirmation", password).toString();
    }

    @Test
    void googleCreatesParticipantAndUsesSubjectOnLaterLogins() throws Exception {
        String path = "/v1/api/auth/google";
        String email = "social-new@gmail.com";
        String credential = "first-google-credential";
        prepareGoogle(credential, "google-first-subject", email, true);
        String body = json.createObjectNode().put("credential", credential).toString();
        String session = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        User registered = users.findByEmail(email).orElseThrow();
        assertThat(registered.getRole()).isEqualTo(Role.ROLE_CUSTOMER);
        assertThat(registered.getGoogleSubject()).isEqualTo("google-first-subject");
        assertThat(registered.getPhone()).isNull();
        mvc.perform(get("/v1/api/auth/me").header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + json.readTree(session).get("accessToken").asText()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body)), 401, path);

        prepareGoogle("second-credential", "google-first-subject", "changed-google-email@gmail.com", true);
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(json.createObjectNode().put("credential", "second-credential").toString()))
                .andExpect(status().isOk());
        assertThat(users.findByEmail("changed-google-email@gmail.com")).isEmpty();
        assertThat(users.findByEmail(email).orElseThrow().getId()).isEqualTo(registered.getId());
    }

    @Test
    void googleLinksExistingAccountAndRequiresPasswordWhenGoogleDoesNotOwnEmail() throws Exception {
        String path = "/v1/api/auth/google";
        String authoritative = "ticketwave-link@gmail.com";
        bearerFor(authoritative, "11999998001", Role.ROLE_CUSTOMER);
        User existing = users.findByEmail(authoritative).orElseThrow();
        prepareGoogle("authoritative-credential", "google-link-subject", authoritative, true);
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(json.createObjectNode().put("credential", "authoritative-credential").toString()))
                .andExpect(status().isOk());
        User linked = users.findByEmail(authoritative).orElseThrow();
        assertThat(linked.getId()).isEqualTo(existing.getId());
        assertThat(linked.getGoogleSubject()).isEqualTo("google-link-subject");

        String thirdParty = "ticketwave-link@example.test";
        bearerFor(thirdParty, "11999998002", Role.ROLE_CUSTOMER);
        User thirdPartyUser = users.findByEmail(thirdParty).orElseThrow();
        thirdPartyUser.setPassword(passwords.encode("local-password"));
        users.save(thirdPartyUser);
        prepareGoogle("third-party-credential", "google-third-party-subject", thirdParty, false);
        var request = json.createObjectNode().put("credential", "third-party-credential");
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(request.toString())),
                428, path);
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(request.put("password", "wrong-password").toString())), 428, path);
        assertThat(users.findByEmail(thirdParty).orElseThrow().getGoogleSubject()).isNull();
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(request.put("password", "local-password").toString())).andExpect(status().isOk());
        assertThat(users.findByEmail(thirdParty).orElseThrow().getGoogleSubject()).isEqualTo("google-third-party-subject");
    }

    private void prepareGoogle(String credential, String subject, String email, boolean authoritative) throws Exception {
        String clientId = "ticketwave-test.apps.googleusercontent.com";
        when(google.clientId()).thenReturn(clientId);
        String challenge = mvc.perform(get("/v1/api/auth/google/challenge"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andReturn().getResponse().getContentAsString();
        String nonce = json.readTree(challenge).get("nonce").asText();
        when(google.verify(credential)).thenReturn(new GoogleIdentityVerifier.Identity(
                subject, email, "Conta Google Teste", nonce, authoritative));
    }

    @Test
    void createsPfAndPjOrganizationsWithAuthenticatedOwnerAndPrivateDocument() throws Exception {
        String path = "/v1/api/organizations";
        String email = "organization-owner@example.test";
        String ownerToken = bearerFor(email, "11999998210", Role.ROLE_CUSTOMER);
        UUID ownerId = users.findByEmail(email).orElseThrow().getId();
        String individual = """
                {"name":"  Eventos Independentes  ","type":"PF","userId":"00000000-0000-0000-0000-000000000001","role":"ADMIN"}
                """;
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(individual)), 401, path);

        String body = mvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, ownerToken)
                .contentType(MediaType.APPLICATION_JSON).content(individual))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.name").value("Eventos Independentes"))
                .andExpect(jsonPath("$.type").value("PF"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        UUID individualId = UUID.fromString(json.readTree(body).get("id").asText());
        assertThat(organizations.findById(individualId).orElseThrow().getDocumentCiphertext()).isNull();
        assertThat(organizationMembers.findAll()).anySatisfy(member -> {
            assertThat(member.getOrganizationId()).isEqualTo(individualId);
            assertThat(member.getUserId()).isEqualTo(ownerId);
            assertThat(member.getRole()).isEqualTo(OrganizationMemberRole.OWNER);
        });

        for (String document : List.of("529.982.247-25", "11.222.333/0001-81", "12.abc.345/01de-35")) {
            String type = document.length() == 14 ? "PF" : "PJ";
            String company = json.createObjectNode().put("name", "Organização com documento")
                    .put("type", type).put("document", document).toString();
            String response = mvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, ownerToken)
                    .contentType(MediaType.APPLICATION_JSON).content(company))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.type").value(type))
                    .andExpect(jsonPath("$.role").value("OWNER")).andExpect(jsonPath("$.document").doesNotExist())
                    .andExpect(jsonPath("$.documentCiphertext").doesNotExist())
                    .andReturn().getResponse().getContentAsString();
            String normalized = document.replaceAll("[./-]", "");
            assertThat(response).doesNotContain(document, normalized);
            UUID organizationId = UUID.fromString(json.readTree(response).get("id").asText());
            byte[] encrypted = organizations.findById(organizationId).orElseThrow().getDocumentCiphertext();
            assertThat(encrypted).hasSize(normalized.length() + 28);
            assertThat(new String(encrypted, StandardCharsets.ISO_8859_1)).doesNotContain(normalized);
        }

        assertThatThrownBy(() -> organizationMembers.saveAndFlush(
                new OrganizationMemberJpaEntity(individualId, ownerId, OrganizationMemberRole.OWNER)))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(users.findById(ownerId).orElseThrow().getRole()).isEqualTo(Role.ROLE_CUSTOMER);
    }

    @Test
    void rejectsInvalidOrganizationsWithoutCreatingMemberships() throws Exception {
        String path = "/v1/api/organizations";
        String token = bearerFor("invalid-org@example.test", "11999998211", Role.ROLE_CUSTOMER);
        long organizationCount = organizations.count();
        long membershipCount = organizationMembers.count();

        for (String body : List.of(
                "{\"name\":\" \",\"type\":\"PF\"}",
                "{\"name\":\"Empresa sem tipo\"}",
                "{\"name\":\"Tipo inválido\",\"type\":\"OUTRO\"}",
                "{\"name\":\"Empresa sem documento\",\"type\":\"PJ\"}",
                "{\"name\":\"Empresa com documento inválido\",\"type\":\"PJ\",\"document\":\"11.222.333/0001-82\"}",
                "{\"name\":\"Pessoa com documento inválido\",\"type\":\"PF\",\"document\":\"111.111.111-11\"}")) {
            expectError(mvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, token)
                    .contentType(MediaType.APPLICATION_JSON).content(body)), 400, path);
        }

        assertThat(organizations.count()).isEqualTo(organizationCount);
        assertThat(organizationMembers.count()).isEqualTo(membershipCount);
    }

    @Test
    void rollsBackOrganizationWhenOwnerMembershipFails() throws Exception {
        String path = "/v1/api/organizations";
        String token = bearerFor("rollback-org@example.test", "11999998212", Role.ROLE_CUSTOMER);
        long organizationCount = organizations.count();
        long membershipCount = organizationMembers.count();

        doAnswer(invocation -> {
            OrganizationMemberJpaEntity member = invocation.getArgument(0);
            assertThat(organizations.findById(member.getOrganizationId())).isPresent();
            throw new IllegalStateException("Falha simulada ao gravar o vínculo");
        }).when(organizationMembers).saveAndFlush(any(OrganizationMemberJpaEntity.class));

        expectError(mvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Organização com falha","type":"PF"}
                        """)), 500, path);
        verify(organizationMembers).saveAndFlush(any(OrganizationMemberJpaEntity.class));
        assertThat(organizations.count()).isEqualTo(organizationCount);
        assertThat(organizationMembers.count()).isEqualTo(membershipCount);
    }

    private String bearerFor(String email, String phone, Role role) {
        User user = new User();
        user.setName("Test user");
        user.setEmail(email);
        user.setPassword("test-hash");
        user.setPhone(phone);
        user.setRole(role);
        return "Bearer " + jwtService.generateToken(new CustomUserDetails(users.save(user)));
    }

    private ResultActions expectError(ResultActions response, int code, String path) throws Exception {
        return response.andExpect(status().is(code))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(code))
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.path").value(path))
                .andExpect(jsonPath("$.violations").isArray());
    }
}
