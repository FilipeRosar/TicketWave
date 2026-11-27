package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, UUID> {
    @Query("""
            SELECT o FROM OrganizationJpaEntity o
            WHERE o.status = 'ACTIVE' AND o.verificationStatus = 'IN_REVIEW'
              AND o.verificationRequestedBy <> :actorId
              AND NOT EXISTS (
                  SELECT 1 FROM OrganizationMemberJpaEntity m
                  WHERE m.organizationId = o.id AND m.userId = :actorId
              )
            ORDER BY o.updatedAt, o.id
            """)
    Page<OrganizationJpaEntity> findVerificationQueue(UUID actorId, Pageable pageable);

    boolean existsByIdAndStatus(UUID id, OrganizationStatus status);

    @Query("""
            SELECT organization FROM OrganizationJpaEntity organization
            WHERE organization.status = 'ACTIVE'
              AND EXISTS (
                  SELECT 1 FROM OrganizationMemberJpaEntity member
                  WHERE member.organizationId = organization.id AND member.userId = :userId
                    AND member.role = 'OWNER' AND member.status = 'ACTIVE'
              )
            ORDER BY organization.createdDate, organization.id
            """)
    List<OrganizationJpaEntity> findActiveOwnedBy(UUID userId);

    @Query("""
            SELECT CASE WHEN COUNT(organization) > 0 THEN true ELSE false END
            FROM OrganizationJpaEntity organization
            WHERE organization.status = 'ACTIVE'
              AND EXISTS (
                  SELECT 1 FROM OrganizationMemberJpaEntity member
                  WHERE member.organizationId = organization.id AND member.userId = :userId
                    AND member.role IN :roles AND member.status = 'ACTIVE'
              )
            """)
    boolean hasManageableEventOrganization(UUID userId, Set<OrganizationMemberRole> roles);

    @Query(value = "SELECT organization_id FROM legacy_event_organization_map WHERE organizer_id = :userId",
            nativeQuery = true)
    Optional<UUID> findLegacyOrganizationId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT organization FROM OrganizationJpaEntity organization WHERE organization.id = :id")
    Optional<OrganizationJpaEntity> findForMembershipChange(UUID id);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT organization FROM OrganizationJpaEntity organization WHERE organization.id = :id")
    Optional<OrganizationJpaEntity> findForFinancialCheck(UUID id);
}
