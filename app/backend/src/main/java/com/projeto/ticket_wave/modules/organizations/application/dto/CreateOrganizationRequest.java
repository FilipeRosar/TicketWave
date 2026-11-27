package com.projeto.ticket_wave.modules.organizations.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull OrganizationType type,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Size(max = 32) String document
) {
    @Override
    public String toString() {
        return "CreateOrganizationRequest[type=" + type + "]";
    }
}
