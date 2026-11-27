package com.projeto.ticket_wave.modules.identity.application.dto;

import java.util.List;

public record ParticipantProfile(
        String name,
        String occupation,
        String city,
        String bio,
        String photoType,
        byte[] photo,
        List<String> interests,
        List<String> goals
) {
}
