package com.projeto.ticket_wave.modules.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Email
        @Size(max = 255)
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 20)
        String password,

        @NotBlank
        @Size(min = 8, max = 15)
        String phone
) {
}
