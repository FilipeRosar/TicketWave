package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;

import java.util.UUID;

public record OrganizationMemberResponse(UUID userId, OrganizationMemberRole role, OrganizationStatus status) {
}
