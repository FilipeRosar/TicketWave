package com.projeto.ticket_wave.modules.events.application.dto;

import java.util.UUID;

public record UpdateVenueRequest(
        String name,
        String description,
        UUID  addressId,
        Boolean active
) {
}
