package com.projeto.ticket_wave.modules.events.application.dto;

import com.projeto.ticket_wave.modules.events.domain.enums.EventClassification;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventSummaryResponse(
        UUID id,

        UUID organizationId,

        String title,

        String slug,

        LocalDateTime startDate,

        BigDecimal ticketPrice,

        String city,

        String category,

        EventClassification classification,

        String shortDescription,

        Boolean featured,

        EventStatus status
) {
}
