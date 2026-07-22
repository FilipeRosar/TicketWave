package com.projeto.ticket_wave.application.dto.EventDTOs;

import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventFilter(

        String title,

        UUID categoryId,

        UUID organizerId,

        EventStatus status,

        EventType type,

        String city,

        LocalDate start,

        LocalDate end,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        Boolean featured,

        Boolean networking,

        Boolean aiAssistant
) {
}
