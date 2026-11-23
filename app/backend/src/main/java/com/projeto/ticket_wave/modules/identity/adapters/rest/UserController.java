package com.projeto.ticket_wave.modules.identity.adapters.rest;

import com.projeto.ticket_wave.modules.identity.application.dto.RegisterUserRequest;
import com.projeto.ticket_wave.modules.identity.application.dto.UserResponse;
import com.projeto.ticket_wave.modules.identity.application.mapper.UserMapper;
import com.projeto.ticket_wave.modules.identity.application.service.UserService;
import com.projeto.ticket_wave.modules.identity.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterUserRequest  registerUserRequest) {
        UserResponse response = userService.createUser(registerUserRequest);
        return new ApiResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Usuário cadastrado com sucesso.",
                response
        );
    }
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId)).getBody();
    }
    @GetMapping("/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return mapper.toDto(userDetails.getUser());
    }
}
