package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.application.dto.EventReportRow;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, UUID>,
        JpaSpecificationExecutor<EventJpaEntity>,
        SlugRepository{
    Optional<EventJpaEntity> findBySlugIgnoreCase(String slug);
    boolean existsBySlug(String slug);
    Page<EventJpaEntity> findByPublishedTrue(Pageable pageable);
    String MANAGED_ORGANIZATION = """
            EXISTS (
                SELECT 1 FROM OrganizationMemberJpaEntity member, OrganizationJpaEntity organization
                WHERE organization.id = e.organizationId AND organization.status = 'ACTIVE'
                  AND member.organizationId = organization.id AND member.userId = :userId
                  AND member.role IN :roles AND member.status = 'ACTIVE'
            )
            """;

    @Query("SELECT e FROM Event e WHERE e.id = :id AND " + MANAGED_ORGANIZATION)
    Optional<EventJpaEntity> findManagedEvent(UUID id, UUID userId, Set<OrganizationMemberRole> roles);

    @Query("SELECT e FROM Event e WHERE e.id = :id AND ((e.published = true AND e.eventStatus = 'PUBLISHED') OR "
            + MANAGED_ORGANIZATION + ")")
    Optional<EventJpaEntity> findVisibleEvent(UUID id, UUID userId, Set<OrganizationMemberRole> roles);

    @Query("SELECT e FROM Event e WHERE (:organizationId IS NULL OR e.organizationId = :organizationId) AND "
            + MANAGED_ORGANIZATION)
    Page<EventJpaEntity> findManageableByMember(
            UUID userId, UUID organizationId, Set<OrganizationMemberRole> roles, Pageable pageable);

    @Query("""
            SELECT new com.projeto.ticket_wave.modules.events.application.dto.EventReportRow(
                e.eventStatus, COUNT(e), SUM(e.totalTicketsSold))
            FROM Event e
            WHERE e.organizationId = :organizationId AND
            """ + MANAGED_ORGANIZATION + " GROUP BY e.eventStatus ORDER BY e.eventStatus")
    List<EventReportRow> report(UUID organizationId, UUID userId, Set<OrganizationMemberRole> roles);

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
