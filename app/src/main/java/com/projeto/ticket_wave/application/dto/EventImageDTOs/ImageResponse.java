package com.projeto.ticket_wave.application.dto.EventImageDTOs;

import java.util.UUID;

public record ImageResponse(
        UUID id,
        String imageUrl,
        Boolean cover,
        Integer displayOrder
) {
}
