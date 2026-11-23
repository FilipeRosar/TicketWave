package com.projeto.ticket_wave.modules.events.application.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,

        String name,

        String slug,

        String description
) {
}
