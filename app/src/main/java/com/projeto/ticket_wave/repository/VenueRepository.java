package com.projeto.ticket_wave.repository;


import com.projeto.ticket_wave.domain.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {
    Optional<Venue> findBySlug(String slug);
    boolean existsBySlugIgnoreCase(String slug);
}
