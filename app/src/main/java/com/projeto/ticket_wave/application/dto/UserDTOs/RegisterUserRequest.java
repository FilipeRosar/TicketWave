package com.projeto.ticket_wave.application.dto.UserDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Email
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8)
        String password
) {
}
