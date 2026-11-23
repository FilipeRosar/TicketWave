package com.projeto.ticket_wave.modules.events.infrastructure.persistence;


import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.VenueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueJpaRepository extends JpaRepository<VenueJpaEntity, UUID> {
    Optional<VenueJpaEntity> findBySlug(String slug);
    boolean existsBySlugIgnoreCase(String slug);
}
