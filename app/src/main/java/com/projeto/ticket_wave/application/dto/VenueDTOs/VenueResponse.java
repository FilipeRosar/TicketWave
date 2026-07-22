package com.projeto.ticket_wave.application.dto.VenueDTOs;

import com.projeto.ticket_wave.application.dto.AddressDTOs.AddressResponse;

import java.util.UUID;

public record VenueResponse(
        UUID id,

        String name,

        String slug,

        String description,

        Boolean active,

        AddressResponse address
) {
}
