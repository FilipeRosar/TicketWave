package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record VerificationSubmission(
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Size(max = 32) String document
) {
    @Override
    public String toString() {
        return "VerificationSubmission";
    }
}
