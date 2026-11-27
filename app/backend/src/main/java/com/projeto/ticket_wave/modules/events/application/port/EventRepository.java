package com.projeto.ticket_wave.modules.events.application.port;

import com.projeto.ticket_wave.modules.events.application.dto.EventReportRow;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.search.EventSearchCriteria;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepository {
    Optional<Event> findById(UUID id);

    Optional<Event> findManagedEvent(UUID id, UUID userId, OrganizationPermission permission);

    Optional<Event> findVisibleEvent(UUID id, UUID userId);

    List<EventReportRow> report(UUID organizationId, UUID userId);

    Optional<Event> findBySlugIgnoreCase(String slug);

    boolean existsBySlug(String slug);

    Page<Event> findPublished(Pageable pageable);

    Page<Event> findManageableByMember(UUID userId, UUID organizationId, Pageable pageable);

    Page<Event> searchPublished(EventSearchCriteria criteria, Pageable pageable);

    Page<Event> search(EventSearchCriteria criteria, Pageable pageable);

    Event save(Event event);

    void delete(Event event);
}
