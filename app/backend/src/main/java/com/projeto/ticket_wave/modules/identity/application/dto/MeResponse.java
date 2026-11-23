package com.projeto.ticket_wave.modules.identity.application.dto;

import com.projeto.ticket_wave.modules.identity.domain.enums.Role;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String name,
        String email,
        Role role,
        Boolean enabled
) {
}
