package com.projeto.ticket_wave.modules.events.application.port;

import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.search.EventSearchCriteria;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository {
    Optional<Event> findById(UUID id);

    Optional<Event> findBySlugIgnoreCase(String slug);

    boolean existsBySlug(String slug);

    Page<Event> findPublished(Pageable pageable);

    Page<Event> search(EventSearchCriteria criteria, Pageable pageable);

    Event save(Event event);

    void delete(Event event);
}
