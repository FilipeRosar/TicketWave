package com.projeto.ticket_wave.application.dto.VenueDTOs;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateVenueRequest(
        @NotBlank
        String name,

        String description,

        UUID addressId
) {
}
