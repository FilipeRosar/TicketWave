package com.projeto.ticket_wave.application.dto.UserDTOs;

import com.projeto.ticket_wave.domain.enums.Role;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String name,
        String email,
        Role role,
        Boolean enabled
) {
}
