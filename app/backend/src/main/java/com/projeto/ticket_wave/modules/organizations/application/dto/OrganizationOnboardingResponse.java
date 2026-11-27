package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationFinancialStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;

import com.projeto.ticket_wave.modules.organizations.domain.VerificationStatus;
import com.projeto.ticket_wave.modules.organizations.domain.VerificationReason;

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
        VerificationStatus verificationStatus,
        VerificationReason verificationReason,
        boolean documentAvailable,
        List<String> pending
) {
}
