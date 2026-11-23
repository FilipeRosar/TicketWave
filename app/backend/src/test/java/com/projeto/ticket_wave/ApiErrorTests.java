package com.projeto.ticket_wave;

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

import java.util.UUID;

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

    @Test
    void formatsValidationAndMalformedRequests() throws Exception {
        String path = "/v1/api/auth/register";
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}")), 400, path)
                .andExpect(jsonPath("$.violations.length()").value(4));
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
                {"name":"Nova conta","email":"duplicate@example.test","password":"test-password","phone":"11933334444"}
                """;
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isOk());
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(registration)), 409, path);
        expectError(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("duplicate@example.test", "another@example.test"))), 409, path);
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
