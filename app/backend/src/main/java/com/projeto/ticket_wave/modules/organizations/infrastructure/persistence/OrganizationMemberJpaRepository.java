package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface OrganizationMemberJpaRepository extends JpaRepository<OrganizationMemberJpaEntity, UUID> {
    boolean existsByOrganizationIdAndUserIdAndRoleInAndStatus(
            UUID organizationId, UUID userId, Set<OrganizationMemberRole> roles, OrganizationStatus status);

    boolean existsByOrganizationIdAndUserIdAndRoleAndStatus(
            UUID organizationId, UUID userId, OrganizationMemberRole role, OrganizationStatus status);
}
