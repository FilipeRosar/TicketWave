package com.projeto.ticket_wave.application.dto.EventDTOs;

import com.projeto.ticket_wave.domain.enums.EventType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventRequest(
        @NotBlank
        String title,

        @NotBlank
        String shortDescription,

        @NotBlank
        String description,

        @Future
        LocalDateTime startDate,

        @Future
        LocalDateTime endDate,

        Integer capacity,

        BigDecimal ticketPrice,

        EventType eventType,

        UUID categoryId,

        UUID venueId,

        Boolean networkingEnabled,

        Boolean gamificationEnabled,

        Boolean aiAssistantEnabled,

        String streamingUrl
) {
}
