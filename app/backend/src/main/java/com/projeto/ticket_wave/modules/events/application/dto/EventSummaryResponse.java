package com.projeto.ticket_wave.modules.events.application.dto;

import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventSummaryResponse(
        UUID id,

        String title,

        String slug,

        LocalDateTime startDate,

        BigDecimal ticketPrice,

        String city,

        Boolean featured,

        EventStatus status
) {
}
