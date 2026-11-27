package com.projeto.ticket_wave.modules.organizations.application.service;

import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.organizations.application.dto.OrganizationOnboardingResponse;
import com.projeto.ticket_wave.modules.organizations.application.dto.UpdateCommercialProfileRequest;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationFinancialStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaEntity;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.security.OrganizationAccess;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.exception.ConflictException;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission.EDIT_DRAFTS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationOnboardingService {
    private final CurrentUser currentUser;
    private final OrganizationJpaRepository organizations;
    private final OrganizationAccess access;

    public List<OrganizationOnboardingResponse> list() {
        return organizations.findActiveOwnedBy(currentUser.getCurrentUser().getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("@organizationAccess.allowed(#organizationId, 'EDIT_COMMERCIAL_PROFILE')")
    public OrganizationOnboardingResponse updateCommercialProfile(UUID organizationId, UpdateCommercialProfileRequest request) {
        var organization = organizations.findById(organizationId).orElseThrow(this::forbidden);
        organization.updateCommercialProfile(clean(request.commercialName()), clean(request.contactEmail()),
                clean(request.description()));
        return toResponse(organization);
    }

    public UUID eventOrganization(UUID requestedId, UUID userId) {
        if (requestedId != null) {
            access.require(requestedId, userId, EDIT_DRAFTS);
            return requestedId;
        }
        var legacyId = organizations.findLegacyOrganizationId(userId);
        if (legacyId.isPresent()) {
            access.require(legacyId.get(), userId, EDIT_DRAFTS);
            return legacyId.get();
        }
        access.requireAny(userId, EDIT_DRAFTS);
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
                organization.getFinancialStatus(), organization.getVerificationStatus(), organization.getVerificationReason(),
                organization.getDocumentCiphertext() != null, List.copyOf(pending));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private ForbiddenException forbidden() {
        return new ForbiddenException("Somente proprietários ativos podem gerenciar esta organização.");
    }
}
