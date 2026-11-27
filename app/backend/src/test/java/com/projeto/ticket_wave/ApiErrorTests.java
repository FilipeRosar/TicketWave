package com.projeto.ticket_wave;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.JwtService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                         "birthDate":"1995-04-20","cpf":"529.982.247-25","passwordConfirmation":"test-password"}
                        """))
                .andExpect(status().isOk());

        User registered = users.findByEmail("jwt-flow@example.test").orElseThrow();
        assertThat(registered.getBirthDate()).isEqualTo(LocalDate.of(1995, 4, 20));
        assertThat(registered.getCpf()).isEqualTo("52998224725");

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
                .andExpect(jsonPath("$.violations.length()").value(7));
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
                 "birthDate":"1995-04-20","cpf":"111.444.777-35","passwordConfirmation":"test-password"}
                """;
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("111.444.777-35", "111.111.111-11"))), 400, path)
                .andExpect(jsonPath("$.violations[0].field").value("cpf"));
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

        user.setRole(Role.ROLE_ORGANIZER);
        User organizer = users.save(user);
        String organizerToken = "Bearer " + jwtService.generateToken(new CustomUserDetails(organizer));
        expectError(mvc.perform(post(eventPath).header(HttpHeaders.AUTHORIZATION, organizerToken)
                .contentType(MediaType.APPLICATION_JSON).content(event.replace("\"capacity\": 100", "\"capacity\": 0"))),
                400, eventPath).andExpect(jsonPath("$.violations[0].field").value("capacity"));
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
