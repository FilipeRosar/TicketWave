package com.projeto.ticket_wave.modules.identity.application.dto;

public record ParticipantProfile(
        String name,
        String occupation,
        String city,
        String bio,
        String photoType,
        byte[] photo
) {
}
