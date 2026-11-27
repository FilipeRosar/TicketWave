package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventImageJpaEntity;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EventImageJpaRepository extends JpaRepository<EventImageJpaEntity, UUID> {
    @Query("SELECT image FROM EventImage image JOIN image.event e WHERE image.id = :id AND "
            + EventJpaRepository.MANAGED_ORGANIZATION)
    Optional<EventImageJpaEntity> findManagedImage(UUID id, UUID userId, Set<OrganizationMemberRole> roles);

    List<EventImageJpaEntity> findByEventId(UUID eventId);
    List<EventImageJpaEntity> findByEventIdOrderByDisplayOrder(UUID eventId);
}
