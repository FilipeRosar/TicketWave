package com.projeto.ticket_wave.modules.organizations.application.service;

import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.modules.organizations.application.dto.OrganizationMemberResponse;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaEntity;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.security.OrganizationAccess;
import com.projeto.ticket_wave.shared.exception.ConflictException;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import com.projeto.ticket_wave.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole.OWNER;
import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus.ACTIVE;

@Service
@RequiredArgsConstructor
public class OrganizationMembershipService {
    private final CurrentUser currentUser;
    private final OrganizationAccess access;
    private final OrganizationJpaRepository organizations;
    private final OrganizationMemberJpaRepository members;
    private final UserRepository users;

    @Transactional(readOnly = true)
    @PreAuthorize("@organizationAccess.allowed(#organizationId, 'READ_MEMBERS')")
    public List<OrganizationMemberResponse> list(UUID organizationId) {
        return members.findAllByOrganizationIdOrderByCreatedDateAscIdAsc(organizationId).stream()
                .map(this::response).toList();
    }

    @Transactional
    @PreAuthorize("@organizationAccess.allowed(#organizationId, 'MANAGE_MEMBERS')")
    public OrganizationMemberResponse assign(UUID organizationId, UUID userId, OrganizationMemberRole role) {
        lockOrganization(organizationId);
        users.findById(userId).filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .orElseThrow(() -> new NotFoundException("Usuário ativo não encontrado."));
        var member = members.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseGet(() -> new OrganizationMemberJpaEntity(organizationId, userId, role));
        if (role != OWNER) requireAnotherOwner(member);
        member.activate(role);
        return response(members.save(member));
    }

    @Transactional
    @PreAuthorize("@organizationAccess.allowed(#organizationId, 'MANAGE_MEMBERS')")
    public void remove(UUID organizationId, UUID userId) {
        lockOrganization(organizationId);
        var member = members.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new NotFoundException("Vínculo não encontrado."));
        requireAnotherOwner(member);
        member.deactivate();
    }

    private void lockOrganization(UUID organizationId) {
        organizations.findForMembershipChange(organizationId)
                .filter(organization -> organization.getStatus() == ACTIVE)
                .orElseThrow(() -> new ForbiddenException("Organização indisponível."));
        // Recheck after acquiring the lock so a concurrent removal cannot leave stale authority.
        access.require(organizationId, currentUser.getCurrentUser().getId(), OrganizationPermission.MANAGE_MEMBERS);
    }

    private void requireAnotherOwner(OrganizationMemberJpaEntity member) {
        if (member.getStatus() == ACTIVE && member.getRole() == OWNER
                && members.countByOrganizationIdAndRoleAndStatus(member.getOrganizationId(), OWNER, ACTIVE) <= 1) {
            throw new ConflictException("A organização deve manter pelo menos um proprietário ativo.");
        }
    }

    private OrganizationMemberResponse response(OrganizationMemberJpaEntity member) {
        return new OrganizationMemberResponse(member.getUserId(), member.getRole(), member.getStatus());
    }
}
