package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;
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
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, UUID>,
        JpaSpecificationExecutor<EventJpaEntity>,
        SlugRepository{
    Optional<EventJpaEntity> findBySlugIgnoreCase(String slug);
    boolean existsBySlug(String slug);
    Page<EventJpaEntity> findByPublishedTrue(Pageable pageable);
    Page<EventJpaEntity> findByOrganizerId(UUID organizerId, Pageable pageable);
    Page<EventJpaEntity> findByCategoryId(UUID categoryId, Pageable pageable);
    Page<EventJpaEntity> findByEventStatus(EventStatus eventStatus, Pageable pageable);

    Page<EventJpaEntity> findByStartDateBetween(Date startDate, Date endDate, Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE e.startDate >= CURRENT_TIMESTAMP
    AND e.published = true
    ORDER BY e.startDate
    """)
    Page<EventJpaEntity> findUpcomingEvents(Pageable pageable);

    @Query("""
    SELECT e
        FROM Event e
        WHERE e.featured = true
        AND e.published = true
            ORDER BY e.startDate
    """)
    Page<EventJpaEntity> findFeaturedEvents(Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
        WHERE e.published = true
        AND e.eventStatus = 'PUBLISHED'
    """)
    Page<EventJpaEntity> findPublished(Pageable pageable);

    @Query("""
    SELECT e
    FROM Event e
    WHERE
        LOWER(e.title) LIKE LOWER(CONCAT('%',:text,'%'))
        OR LOWER(e.shortDescription) LIKE LOWER(CONCAT('%',:text,'%'))
    """)
    Page<EventJpaEntity> search(@Param("text") String text, Pageable pageable);

    Page<EventJpaEntity> findByCategorySlug(String slug, Pageable pageable);

    Page<EventJpaEntity> findByVenueAddressCityIgnoreCase(
            String city,
            Pageable pageable);

    Page<EventJpaEntity> findByTicketPriceBetween(
            BigDecimal min,
            BigDecimal max,
            Pageable pageable);

    Page<EventJpaEntity> findByEventType(
            EventType eventType,
            Pageable pageable);

    Page<EventJpaEntity> findByAiAssistantEnabledTrue(
            Pageable pageable);

    Page<EventJpaEntity> findByNetworkingEnabledTrue(
            Pageable pageable);

    Page<EventJpaEntity> findByGamificationEnabledTrue(
            Pageable pageable);

}
