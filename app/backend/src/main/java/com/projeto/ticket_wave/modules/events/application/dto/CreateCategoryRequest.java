package com.projeto.ticket_wave.modules.events.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank
        String name,

        String description
) {
}
