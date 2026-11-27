package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;
import com.projeto.ticket_wave.modules.organizations.domain.VerificationReason;
import com.projeto.ticket_wave.modules.organizations.domain.VerificationStatus;

import java.util.UUID;

public record VerificationSummary(UUID id, String name, OrganizationType type, VerificationStatus status,
                                  VerificationReason reason, boolean documentAvailable) {
}
