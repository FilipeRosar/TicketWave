package com.projeto.ticket_wave.modules.organizations.application.service;

import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.organizations.application.dto.VerificationDecision;
import com.projeto.ticket_wave.modules.organizations.application.dto.VerificationSubmission;
import com.projeto.ticket_wave.modules.organizations.application.dto.VerificationSummary;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationDocument;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;
import com.projeto.ticket_wave.modules.organizations.domain.VerificationStatus;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.*;
import com.projeto.ticket_wave.modules.organizations.infrastructure.security.OrganizationAccess;
import com.projeto.ticket_wave.modules.organizations.infrastructure.security.OrganizationDocumentEncryption;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.exception.ConflictException;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import com.projeto.ticket_wave.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationVerificationService {
    private final CurrentUser currentUser;
    private final OrganizationAccess access;
    private final OrganizationJpaRepository organizations;
    private final OrganizationMemberJpaRepository members;
    private final OrganizationDocumentEncryption encryption;
    private final VerificationHistoryJpaRepository history;

    @Transactional
    @PreAuthorize("@organizationAccess.allowed(#id, 'EDIT_COMMERCIAL_PROFILE')")
    public VerificationSummary submit(UUID id, VerificationSubmission request) {
        var organization = lock(id);
        UUID actor = currentUser.getCurrentUser().getId();
        access.require(id, actor, OrganizationPermission.EDIT_COMMERCIAL_PROFILE);
        var before = organization.getVerificationStatus();
        if (before != VerificationStatus.PENDING && before != VerificationStatus.REJECTED) {
            throw new ConflictException("Somente verificações pendentes ou rejeitadas podem ser enviadas.");
        }
        String document = request.document();
        byte[] replacement = null;
        if (document != null && !document.isBlank()) {
            replacement = encryption.encrypt(OrganizationDocument.normalize(organization.getType(), document));
        } else if (organization.getType() == OrganizationType.PJ && organization.getDocumentCiphertext() == null) {
            throw new BusinessException("Informe o CNPJ para enviar a verificação.");
        }
        organization.submitVerification(actor, replacement);
        history.save(new VerificationHistoryJpaEntity(id, actor, before, VerificationStatus.IN_REVIEW, null));
        return summary(organization);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public VerificationSummary review(UUID id, VerificationDecision request) {
        var organization = lock(id);
        UUID actor = currentUser.getCurrentUser().getId();
        if (actor.equals(organization.getVerificationRequestedBy())
                || members.existsByOrganizationIdAndUserId(id, actor)) {
            throw new ForbiddenException("A decisão exige um revisor independente da organização.");
        }
        if (organization.getVerificationStatus() != VerificationStatus.IN_REVIEW) {
            throw new ConflictException("Somente verificações em análise podem ser decididas.");
        }
        var result = request.status();
        if (result != VerificationStatus.APPROVED && result != VerificationStatus.REJECTED) {
            throw new BusinessException("A decisão deve aprovar ou rejeitar a verificação.");
        }
        if ((result == VerificationStatus.REJECTED) != (request.reason() != null)) {
            throw new BusinessException("Informe um motivo apenas ao rejeitar a verificação.");
        }
        organization.decideVerification(result, request.reason());
        history.save(new VerificationHistoryJpaEntity(id, actor, VerificationStatus.IN_REVIEW, result, request.reason()));
        return summary(organization);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<VerificationSummary> queue(Pageable pageable) {
        return organizations.findVerificationQueue(currentUser.getCurrentUser().getId(), pageable).map(this::summary);
    }

    private OrganizationJpaEntity lock(UUID id) {
        return organizations.findForMembershipChange(id)
                .filter(organization -> organization.getStatus() == OrganizationStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Organização indisponível."));
    }

    private VerificationSummary summary(OrganizationJpaEntity organization) {
        return new VerificationSummary(organization.getId(), organization.getName(), organization.getType(),
                organization.getVerificationStatus(), organization.getVerificationReason(),
                organization.getDocumentCiphertext() != null);
    }
}
