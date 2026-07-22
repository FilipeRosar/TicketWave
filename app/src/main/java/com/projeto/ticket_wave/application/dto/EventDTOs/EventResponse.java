package com.projeto.ticket_wave.application.dto.EventDTOs;

import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventResponse(
        UUID id,

        String title,

        String slug,

        String shortDescription,

        String description,

        LocalDateTime startDate,

        LocalDateTime endDate,

        Integer capacity,

        Integer availableTickets,

        BigDecimal ticketPrice,

        EventStatus eventStatus,

        EventType eventType,

        Boolean published,

        Boolean featured,

        Boolean networkingEnabled,

        Boolean gamificationEnabled,

        Boolean aiAssistantEnabled,

        String streamingUrl,

        String organizer,

        String category,

        String venue
) {
}
