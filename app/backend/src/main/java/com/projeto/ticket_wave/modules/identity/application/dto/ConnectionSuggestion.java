package com.projeto.ticket_wave.modules.identity.application.dto;

import java.util.List;
import java.util.UUID;

public record ConnectionSuggestion(
        UUID userId,
        String name,
        String occupation,
        String city,
        List<String> sharedInterests,
        List<String> matchedGoals
) {
}
