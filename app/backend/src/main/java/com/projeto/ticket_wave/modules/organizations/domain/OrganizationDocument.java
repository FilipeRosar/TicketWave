package com.projeto.ticket_wave.modules.organizations.domain;

import com.projeto.ticket_wave.shared.exception.BusinessException;

import java.util.Locale;

public final class OrganizationDocument {
    private OrganizationDocument() {
    }

    public static String normalize(OrganizationType type, String document) {
        if (document == null || document.isBlank()) {
            if (type == OrganizationType.PJ) throw new BusinessException("Informe o CNPJ da organização.");
            return null;
        }
        String value = document.strip();
        return switch (type) {
            case PF -> cpf(value);
            case PJ -> cnpj(value);
        };
    }

    private static String cpf(String value) {
        if (!value.matches("[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}")) {
            throw new BusinessException("Informe um CPF válido.");
        }
        String digits = value.replace(".", "").replace("-", "");
        if (digits.chars().distinct().count() == 1
                || digits.charAt(9) - '0' != cpfCheckDigit(digits, 9)
                || digits.charAt(10) - '0' != cpfCheckDigit(digits, 10)) {
            throw new BusinessException("Informe um CPF válido.");
        }
        return digits;
    }

    private static int cpfCheckDigit(String digits, int length) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            sum += (digits.charAt(index) - '0') * (length + 1 - index);
        }
        return checkDigit(sum);
    }

    private static String cnpj(String value) {
        if (!value.matches("[A-Za-z0-9]{12}[0-9]{2}|[A-Za-z0-9]{2}\\.[A-Za-z0-9]{3}\\.[A-Za-z0-9]{3}/[A-Za-z0-9]{4}-[0-9]{2}")) {
            throw new BusinessException("Informe um CNPJ válido.");
        }
        String normalized = value.replace(".", "").replace("/", "").replace("-", "").toUpperCase(Locale.ROOT);
        if (normalized.chars().distinct().count() == 1
                || normalized.charAt(12) - '0' != cnpjCheckDigit(normalized, 12)
                || normalized.charAt(13) - '0' != cnpjCheckDigit(normalized, 13)) {
            throw new BusinessException("Informe um CNPJ válido.");
        }
        return normalized;
    }

    private static int cnpjCheckDigit(String value, int length) {
        int sum = 0;
        for (int index = 0; index < length; index++) {
            int weight = 2 + (length - 1 - index) % 8;
            sum += (value.charAt(index) - '0') * weight;
        }
        return checkDigit(sum);
    }

    private static int checkDigit(int sum) {
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
