package com.projeto.ticket_wave.modules.events.application.dto;

import com.projeto.ticket_wave.modules.events.domain.enums.EventClassification;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;

import com.projeto.ticket_wave.modules.events.domain.enums.NetworkingFeature;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

        EventClassification classification,

        Boolean published,

        Boolean featured,

        Boolean networkingEnabled,

        List<NetworkingFeature> networkingFeatures,

        Boolean gamificationEnabled,

        Boolean aiAssistantEnabled,

        String streamingUrl,

        String organizer,

        String category,

        String venue
) {
}
