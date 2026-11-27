package com.projeto.ticket_wave.modules.identity.application.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "Link inválido ou expirado.") String token,
        @NotBlank @Size(min = 8, max = 20) String password,
        @NotBlank @Size(min = 8, max = 20) String passwordConfirmation
) {
    @AssertTrue(message = "As senhas devem ser iguais.")
    public boolean isPasswordConfirmed() {
        return password == null || passwordConfirmation == null || password.equals(passwordConfirmation);
    }
}
