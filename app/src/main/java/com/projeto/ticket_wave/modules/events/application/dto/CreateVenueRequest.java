package com.projeto.ticket_wave.modules.events.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateVenueRequest(
        @NotBlank
        String name,

        String description,

        UUID addressId
) {
}
