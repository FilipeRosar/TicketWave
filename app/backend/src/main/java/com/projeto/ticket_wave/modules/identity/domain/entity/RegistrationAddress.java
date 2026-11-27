package com.projeto.ticket_wave.modules.identity.domain.entity;

public record RegistrationAddress(
        String zipCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state
) {
}
