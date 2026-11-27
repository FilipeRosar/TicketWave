package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;

import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        OrganizationType type,
        OrganizationStatus status,
        OrganizationMemberRole role
) {
}
