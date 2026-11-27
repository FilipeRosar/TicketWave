package com.projeto.ticket_wave.modules.organizations.application.service;

import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.modules.organizations.application.dto.CreateOrganizationRequest;
import com.projeto.ticket_wave.modules.organizations.application.dto.OrganizationResponse;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationDocument;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaEntity;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaEntity;
import com.projeto.ticket_wave.modules.organizations.infrastructure.persistence.OrganizationMemberJpaRepository;
import com.projeto.ticket_wave.modules.organizations.infrastructure.security.OrganizationDocumentEncryption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationRegistrationService {
    private final CurrentUser currentUser;
    private final OrganizationDocumentEncryption encryption;
    private final OrganizationJpaRepository organizations;
    private final OrganizationMemberJpaRepository members;

    @Transactional
    public OrganizationResponse create(CreateOrganizationRequest request) {
        var ownerId = currentUser.getCurrentUser().getId();
        String document = OrganizationDocument.normalize(request.type(), request.document());
        var organization = organizations.saveAndFlush(new OrganizationJpaEntity(
                request.name().strip(), request.type(), encryption.encrypt(document)));
        var membership = members.saveAndFlush(new OrganizationMemberJpaEntity(
                organization.getId(), ownerId, OrganizationMemberRole.OWNER));
        return new OrganizationResponse(organization.getId(), organization.getName(), organization.getType(),
                organization.getStatus(), membership.getRole());
    }
}
