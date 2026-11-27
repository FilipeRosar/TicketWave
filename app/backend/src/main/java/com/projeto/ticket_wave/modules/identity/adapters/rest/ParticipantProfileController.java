package com.projeto.ticket_wave.modules.identity.adapters.rest;

import com.projeto.ticket_wave.modules.identity.application.dto.ParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.dto.UpdateParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.service.ParticipantProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/profile/me")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class ParticipantProfileController {
    private final ParticipantProfileService profiles;

    @GetMapping
    public ResponseEntity<ParticipantProfile> get() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(profiles.get());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParticipantProfile> save(@Valid @ModelAttribute UpdateParticipantProfile request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(profiles.save(request));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        profiles.delete();
    }
}
