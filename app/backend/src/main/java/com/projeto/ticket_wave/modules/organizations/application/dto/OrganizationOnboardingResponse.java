package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationFinancialStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;

import java.util.List;
import java.util.UUID;

public record OrganizationOnboardingResponse(
        UUID id,
        String name,
        OrganizationType type,
        String commercialName,
        String contactEmail,
        String description,
        OrganizationFinancialStatus financialStatus,
        List<String> pending
) {
}
