package com.projeto.ticket_wave.application.dto.UserDTOs;

import com.projeto.ticket_wave.domain.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role,
        Instant createdAt
) {
}
