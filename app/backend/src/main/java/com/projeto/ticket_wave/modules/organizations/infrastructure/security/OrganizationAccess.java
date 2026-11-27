package com.projeto.ticket_wave.modules.organizations.infrastructure.security;

import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaRepository;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("organizationAccess")
@RequiredArgsConstructor
public class OrganizationAccess {
    private final CurrentUser currentUser;
    private final OrganizationJpaRepository organizations;
    private final OrganizationMemberJpaRepository members;

    public boolean allowed(UUID organizationId, String permission) {
        return allowed(organizationId, currentUser.getCurrentUser().getId(), OrganizationPermission.valueOf(permission));
    }

    public boolean allowed(UUID organizationId, UUID userId, OrganizationPermission permission) {
        return members.existsByOrganizationIdAndUserIdAndRoleInAndStatus(
                organizationId, userId, permission.roles(), OrganizationStatus.ACTIVE)
                && organizations.existsByIdAndStatus(organizationId, OrganizationStatus.ACTIVE);
    }

    public void require(UUID organizationId, UUID userId, OrganizationPermission permission) {
        if (!allowed(organizationId, userId, permission)) {
            throw new ForbiddenException("Você não tem permissão para esta operação na organização.");
        }
    }

    public void requireAny(UUID userId, OrganizationPermission permission) {
        if (!organizations.hasManageableEventOrganization(userId, permission.roles())) {
            throw new ForbiddenException("Você não tem uma organização com permissão para esta operação.");
        }
    }
}
