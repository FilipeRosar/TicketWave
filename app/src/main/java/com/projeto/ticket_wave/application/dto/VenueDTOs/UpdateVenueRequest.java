package com.projeto.ticket_wave.application.dto.VenueDTOs;

import java.util.UUID;

public record UpdateVenueRequest(
        String name,
        String description,
        UUID  addressId,
        Boolean active
) {
}
