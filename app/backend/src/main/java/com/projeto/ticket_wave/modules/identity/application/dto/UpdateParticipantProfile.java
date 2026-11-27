package com.projeto.ticket_wave.modules.identity.application.dto;

import com.projeto.ticket_wave.modules.identity.domain.enums.ConnectionGoal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UpdateParticipantProfile(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 120) String occupation,
        @Size(max = 120) String city,
        @Size(max = 1000) String bio,
        MultipartFile photo,
        Boolean removePhoto,
        @Size(max = 10, message = "Adicione até 10 interesses.")
        List<@NotBlank @Size(max = 40, message = "Cada interesse deve ter até 40 caracteres.") String> interests,
        @Size(max = 6)
        List<@NotNull ConnectionGoal> goals
) {
}
