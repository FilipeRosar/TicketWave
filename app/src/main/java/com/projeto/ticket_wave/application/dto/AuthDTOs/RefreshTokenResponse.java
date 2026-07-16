package com.projeto.ticket_wave.application.dto.AuthDTOs;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
