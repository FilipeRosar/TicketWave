package com.projeto.ticket_wave.repository;

import com.projeto.ticket_wave.domain.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, UUID> {
    List<EventImage> findByEventId(UUID eventId);
}
