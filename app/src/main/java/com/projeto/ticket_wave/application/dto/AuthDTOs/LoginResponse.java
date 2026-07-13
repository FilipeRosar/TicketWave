package com.projeto.ticket_wave.application.dto.AuthDTOs;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
