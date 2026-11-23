package com.projeto.ticket_wave.modules.identity.application.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
