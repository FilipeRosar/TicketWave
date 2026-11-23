package com.projeto.ticket_wave.modules.events.domain.search;

import com.projeto.ticket_wave.modules.events.domain.enums.EventSort;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record EventSearchCriteria(

        String keyword,

        UUID categoryId,

        UUID organizerId,

        EventStatus eventStatus,

        EventType eventType,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        Boolean featured,

        Boolean networking,

        Boolean aiAssistant,

        Boolean gamification,

        LocalDate startDate,

        LocalDate endDate,

        String city,

        Double latitude,

        Double longitude,

        Double radiusKm,

        Boolean availableOnly,

        EventSort sort
) {
}
