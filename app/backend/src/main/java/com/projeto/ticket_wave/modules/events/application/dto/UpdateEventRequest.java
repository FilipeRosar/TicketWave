package com.projeto.ticket_wave.modules.events.application.dto;

import com.projeto.ticket_wave.modules.events.domain.enums.EventType;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateEventRequest(

        @Pattern(regexp = "(?s).*\\S.*", message = "não pode estar em branco")
        @Size(max = 150)
        String title,

        @Pattern(regexp = "(?s).*\\S.*", message = "não pode estar em branco")
        @Size(max = 500)
        String shortDescription,

        @Pattern(regexp = "(?s).*\\S.*", message = "não pode estar em branco")
        String description,

        @Future
        LocalDateTime startDate,

        @Future
        LocalDateTime endDate,

        @Positive
        Integer capacity,

        @Positive
        @Digits(integer = 8, fraction = 2)
        BigDecimal ticketPrice,

        EventType eventType,

        UUID categoryId,

        UUID venueId,

        Boolean networkingEnabled,

        Boolean gamificationEnabled,

        Boolean aiAssistantEnabled,

        Boolean featured,

        @Size(max = 255)
        String streamingUrl
) {
}
