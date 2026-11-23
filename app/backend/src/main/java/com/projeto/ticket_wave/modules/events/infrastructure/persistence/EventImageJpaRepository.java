package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventImageJpaRepository extends JpaRepository<EventImageJpaEntity, UUID> {
    List<EventImageJpaEntity> findByEventId(UUID eventId);
    List<EventImageJpaEntity> findByEventIdOrderByDisplayOrder(UUID eventId);
}
