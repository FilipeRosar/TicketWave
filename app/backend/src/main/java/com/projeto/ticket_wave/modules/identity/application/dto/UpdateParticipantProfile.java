package com.projeto.ticket_wave.modules.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UpdateParticipantProfile(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 120) String occupation,
        @Size(max = 120) String city,
        @Size(max = 1000) String bio,
        MultipartFile photo,
        Boolean removePhoto
) {
}
