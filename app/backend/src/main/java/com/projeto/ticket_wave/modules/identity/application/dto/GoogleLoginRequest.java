package com.projeto.ticket_wave.modules.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank @Size(max = 8192) String credential,
        @Size(max = 20) String password
) {
}
