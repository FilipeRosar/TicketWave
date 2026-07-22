package com.projeto.ticket_wave.repository;

import com.projeto.ticket_wave.domain.entity.Event;
import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>,
        JpaSpecificationExecutor<Event>,
        SlugRepository{
    Optional<Event> findBySlugIgnoreCase(String slug);
    boolean existsBySlug(String slug);
    Page<Event> findByPublishedTrue(Pageable pageable);
    Page<Event> findByOrganizerId(UUID organizerId, Pageable pageable);
    Page<Event> findByCategoryId(UUID categoryId, Pageable pageable);
    Page<Event> findByEventStatus(EventStatus eventStatus, Pageable pageable);

    Page<Event> findByStartDateBetween(Date startDate, Date endDate, Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.startDate >= CURRENT_TIMESTAMP
    AND e.published = true
    ORDER BY e.startDate
    """)
    Page<Event> findUpcomingEvents(Pageable pageable);

    @Query("""
    SELECT e
        FROM Event e
        WHERE e.featured = true
        AND e.published = true
            ORDER BY e.startDate
    """)
    Page<Event> findFeaturedEvents(Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
        WHERE e.published = true
        AND e.eventStatus = 'PUBLISHED'
    """)
    Page<Event> findPublished(Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE
        LOWER(e.title) LIKE LOWER(CONCAT('%',:text,'%'))
        OR LOWER(e.shortDescription) LIKE LOWER(CONCAT('%',:text,'%'))
    """)
    Page<Event> search(@Param("text") String text, Pageable pageable);

    Page<Event> findByCategorySlug(String slug, Pageable pageable);

    Page<Event> findByVenueAddressCityIgnoreCase(
            String city,
            Pageable pageable);

    Page<Event> findByTicketPriceBetween(
            BigDecimal min,
            BigDecimal max,
            Pageable pageable);

    Page<Event> findByEventType(
            EventType eventType,
            Pageable pageable);

    Page<Event> findByAiAssistantEnabledTrue(
            Pageable pageable);

    Page<Event> findByNetworkingEnabledTrue(
            Pageable pageable);

    Page<Event> findByGamificationEnabledTrue(
            Pageable pageable);

}
