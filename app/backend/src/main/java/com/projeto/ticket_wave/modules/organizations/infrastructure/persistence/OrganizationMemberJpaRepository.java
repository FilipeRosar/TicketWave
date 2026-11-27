package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface OrganizationMemberJpaRepository extends JpaRepository<OrganizationMemberJpaEntity, UUID> {
    List<OrganizationMemberJpaEntity> findAllByOrganizationIdOrderByCreatedDateAscIdAsc(UUID organizationId);

    Optional<OrganizationMemberJpaEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    long countByOrganizationIdAndRoleAndStatus(UUID organizationId, OrganizationMemberRole role, OrganizationStatus status);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndRoleInAndStatus(
            UUID organizationId, UUID userId, Set<OrganizationMemberRole> roles, OrganizationStatus status);

    boolean existsByOrganizationIdAndUserIdAndRoleAndStatus(
            UUID organizationId, UUID userId, OrganizationMemberRole role, OrganizationStatus status);
}
