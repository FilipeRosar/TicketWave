package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateOrganizationMemberRequest(@NotNull OrganizationMemberRole role) {
}
