package com.projeto.ticket_wave.application.dto.CategoryDTOs;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank
        String name,

        String description
) {
}
