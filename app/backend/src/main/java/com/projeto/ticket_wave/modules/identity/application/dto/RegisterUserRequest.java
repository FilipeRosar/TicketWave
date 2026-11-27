package com.projeto.ticket_wave.modules.identity.application.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record RegisterUserRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        @Email
        @Size(max = 255)
        @NotBlank
        String email,

        @NotBlank
        @Size(min = 8, max = 20)
        String password,

        @NotBlank
        @Size(min = 8, max = 15)
        String phone,

        @NotNull(message = "Informe sua data de nascimento.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate birthDate,

        @NotBlank(message = "Informe seu CPF.")
        @CPF(message = "Informe um CPF válido.")
        String cpf,

        @NotBlank(message = "Confirme sua senha.")
        @Size(min = 8, max = 20, message = "A confirmação deve ter de 8 a 20 caracteres.")
        String passwordConfirmation
) {
    @AssertTrue(message = "As senhas devem ser iguais.")
    public boolean isPasswordConfirmed() {
        return password == null || passwordConfirmation == null || password.equals(passwordConfirmation);
    }
}
