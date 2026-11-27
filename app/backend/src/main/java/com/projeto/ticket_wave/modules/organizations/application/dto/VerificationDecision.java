package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.projeto.ticket_wave.modules.organizations.domain.VerificationReason;
import com.projeto.ticket_wave.modules.organizations.domain.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public record VerificationDecision(@NotNull VerificationStatus status, VerificationReason reason) {
}
