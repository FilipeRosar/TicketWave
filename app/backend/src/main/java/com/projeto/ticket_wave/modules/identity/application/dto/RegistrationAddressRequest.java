package com.projeto.ticket_wave.modules.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationAddressRequest(
        @NotBlank(message = "Informe o CEP.")
        @Pattern(regexp = "[0-9]{5}-?[0-9]{3}", message = "Informe um CEP com 8 dígitos.")
        String zipCode,

        @NotBlank(message = "Informe o logradouro.") @Size(max = 255)
        String street,

        @NotBlank(message = "Informe o número ou S/N.") @Size(max = 20)
        String number,

        @Size(max = 150)
        String complement,

        @NotBlank(message = "Informe o bairro.") @Size(max = 150)
        String district,

        @NotBlank(message = "Informe a cidade.") @Size(max = 120)
        String city,

        @NotBlank(message = "Informe a UF.")
        @Pattern(regexp = "AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO",
                message = "Informe uma UF brasileira válida.")
        String state
) {
}
