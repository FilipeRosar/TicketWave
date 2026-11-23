package com.projeto.ticket_wave.modules.identity.application.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
) {
}
