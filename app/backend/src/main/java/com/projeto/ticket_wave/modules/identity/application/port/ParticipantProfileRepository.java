package com.projeto.ticket_wave.modules.identity.application.port;

import com.projeto.ticket_wave.modules.identity.application.dto.ParticipantProfile;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantProfileRepository {
    Optional<ParticipantProfile> findByUserId(UUID userId);
    ParticipantProfile save(UUID userId, ParticipantProfile profile, boolean replacePhoto);
    void deleteByUserId(UUID userId);
}
