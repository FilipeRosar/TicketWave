package com.projeto.ticket_wave.modules.events.application.dto;

import java.util.UUID;

public record ImageResponse(
        UUID id,
        String imageUrl,
        Boolean cover,
        Integer displayOrder
) {
}
