package com.projeto.ticket_wave.application.dto.AddressDTOs;

import java.util.UUID;

public record AddressResponse(
        UUID id,

        String street,

        String number,

        String district,

        String city,

        String state,

        String zipCode,

        String country
) {
}
