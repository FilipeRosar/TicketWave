package com.projeto.ticket_wave.modules.events.application.port;

import com.projeto.ticket_wave.modules.events.domain.entity.Venue;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepository {
    Optional<Venue> findById(UUID id);
}
