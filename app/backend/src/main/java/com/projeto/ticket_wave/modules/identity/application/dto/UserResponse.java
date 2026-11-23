package com.projeto.ticket_wave.modules.identity.application.dto;

import com.projeto.ticket_wave.modules.identity.domain.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String phone,
        Role role,
        Instant createdAt
) {
}
