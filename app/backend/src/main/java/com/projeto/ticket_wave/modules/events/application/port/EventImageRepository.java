package com.projeto.ticket_wave.modules.events.application.port;

import com.projeto.ticket_wave.modules.events.domain.entity.EventImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventImageRepository {
    Optional<EventImage> findManagedImage(UUID id, UUID userId);

    List<EventImage> findByEventId(UUID eventId);

    List<EventImage> findByEventIdOrderByDisplayOrder(UUID eventId);

    EventImage save(EventImage image);

    void saveAll(List<EventImage> images);

    void delete(EventImage image);
}
