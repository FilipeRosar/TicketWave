package com.projeto.ticket_wave.modules.organizations.application.service;

import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.organizations.application.dto.OrganizationOnboardingResponse;
import com.projeto.ticket_wave.modules.organizations.application.dto.UpdateCommercialProfileRequest;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationFinancialStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaEntity;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaRepository;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.exception.ConflictException;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole.EVENT_MANAGERS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationOnboardingService {
    private final CurrentUser currentUser;
    private final OrganizationJpaRepository organizations;
    private final OrganizationMemberJpaRepository members;

    public List<OrganizationOnboardingResponse> list() {
        return organizations.findActiveOwnedBy(currentUser.getCurrentUser().getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public OrganizationOnboardingResponse updateCommercialProfile(UUID organizationId, UpdateCommercialProfileRequest request) {
        var organization = requireOwner(organizationId, currentUser.getCurrentUser().getId());
        organization.updateCommercialProfile(clean(request.commercialName()), clean(request.contactEmail()),
                clean(request.description()));
        return toResponse(organization);
    }

    public OrganizationJpaEntity requireOwner(UUID organizationId, UUID userId) {
        if (hasOwnerMembership(organizationId, userId)) {
            return organizations.findById(organizationId)
                    .filter(organization -> organization.getStatus() == OrganizationStatus.ACTIVE)
                    .orElseThrow(this::forbidden);
        }
        throw forbidden();
    }

    private boolean hasOwnerMembership(UUID organizationId, UUID userId) {
        return members.existsByOrganizationIdAndUserIdAndRoleAndStatus(
                organizationId, userId, OrganizationMemberRole.OWNER, OrganizationStatus.ACTIVE);
    }

    public boolean canManageEvents(UUID organizationId, UUID userId) {
        return members.existsByOrganizationIdAndUserIdAndRoleInAndStatus(
                organizationId, userId, EVENT_MANAGERS, OrganizationStatus.ACTIVE)
                && organizations.existsByIdAndStatus(organizationId, OrganizationStatus.ACTIVE);
    }

    public void requireEventManagement(UUID organizationId, UUID userId) {
        if (!canManageEvents(organizationId, userId)) {
            throw new ForbiddenException("Você não tem permissão para gerenciar eventos desta organização.");
        }
    }

    public void requireAnyEventOrganization(UUID userId) {
        if (!organizations.hasManageableEventOrganization(userId, EVENT_MANAGERS)) {
            throw new ForbiddenException("Você não tem uma organização com permissão para gerenciar eventos.");
        }
    }

    public UUID eventOrganization(UUID requestedId, UUID userId) {
        if (requestedId != null) {
            requireEventManagement(requestedId, userId);
            return requestedId;
        }
        var legacyId = organizations.findLegacyOrganizationId(userId);
        if (legacyId.isPresent()) {
            requireEventManagement(legacyId.get(), userId);
            return legacyId.get();
        }
        requireAnyEventOrganization(userId);
        throw new BusinessException("Informe a organização do evento.");
    }

    @Transactional
    public void requireFinancialApproval(UUID organizationId) {
        if (organizations.findForFinancialCheck(organizationId)
                .filter(organization -> organization.getStatus() == OrganizationStatus.ACTIVE)
                .map(OrganizationJpaEntity::getFinancialStatus).orElse(null) != OrganizationFinancialStatus.APPROVED) {
            throw new ConflictException("A publicação paga exige uma organização com habilitação financeira aprovada.");
        }
    }

    private OrganizationOnboardingResponse toResponse(OrganizationJpaEntity organization) {
        List<String> pending = new ArrayList<>();
        if (organization.getCommercialName() == null) pending.add("COMMERCIAL_NAME");
        if (organization.getContactEmail() == null) pending.add("CONTACT_EMAIL");
        if (organization.getFinancialStatus() != OrganizationFinancialStatus.APPROVED) pending.add("FINANCIAL_APPROVAL");
        return new OrganizationOnboardingResponse(organization.getId(), organization.getName(), organization.getType(),
                organization.getCommercialName(), organization.getContactEmail(), organization.getCommercialDescription(),
                organization.getFinancialStatus(), List.copyOf(pending));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private ForbiddenException forbidden() {
        return new ForbiddenException("Somente proprietários ativos podem gerenciar esta organização.");
    }
}
