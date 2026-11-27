package com.projeto.ticket_wave.modules.organizations.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateCommercialProfileRequest(
        @Size(max = 150) String commercialName,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 1000) String description
) {
}
