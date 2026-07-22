package com.projeto.ticket_wave.application.dto.EventDTOs;

import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EventFilterRequest(
        String title,

        String city,

        String state,

        String category,

        String status,

        EventStatus eventStatus,

        EventType eventType,

        Boolean featured,

        Integer availableTickets,

        Boolean networking,

        Boolean aiAssistant,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        LocalDate startDate,

        LocalDate endDate
) {
}
