package com.projeto.ticket_wave.modules.events.application.dto;

import com.projeto.ticket_wave.modules.events.domain.enums.EventType;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 500)
        String shortDescription,

        @NotBlank
        String description,

        @NotNull
        @Future
        LocalDateTime startDate,

        @NotNull
        @Future
        LocalDateTime endDate,

        @NotNull
        @Positive
        Integer capacity,

        @NotNull
        @Positive
        @Digits(integer = 8, fraction = 2)
        BigDecimal ticketPrice,

        @NotNull
        EventType eventType,

        @NotNull
        UUID categoryId,

        @NotNull
        UUID venueId,

        Boolean networkingEnabled,

        Boolean gamificationEnabled,

        Boolean aiAssistantEnabled,

        @Size(max = 255)
        String streamingUrl
) {
}
