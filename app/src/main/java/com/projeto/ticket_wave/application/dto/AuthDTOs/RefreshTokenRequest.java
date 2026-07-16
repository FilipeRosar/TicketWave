package com.projeto.ticket_wave.application.dto.AuthDTOs;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank
        String refreshToken
) {
}
