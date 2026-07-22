package com.projeto.ticket_wave.application.dto.EventDTOs;

import com.projeto.ticket_wave.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateEventRequest(

        String title,

        String shortDescription,

        String description,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Integer capacity,

        BigDecimal ticketPrice,

        EventType eventType,

        UUID categoryId,

        UUID venueId,

        Boolean networkingEnabled,

        Boolean gamificationEnabled,

        Boolean aiAssistantEnabled,

        Boolean featured,

        String streamingUrl
) {
}
