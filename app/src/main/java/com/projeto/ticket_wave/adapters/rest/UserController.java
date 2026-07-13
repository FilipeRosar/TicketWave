package com.projeto.ticket_wave.adapters.rest;

import com.projeto.ticket_wave.application.dto.UserDTOs.RegisterUserRequest;
import com.projeto.ticket_wave.application.dto.UserDTOs.UserResponse;
import com.projeto.ticket_wave.application.dto.response.ApiResponse;
import com.projeto.ticket_wave.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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
}
