package com.projeto.ticket_wave.modules.organizations.adapters.rest;

import com.projeto.ticket_wave.modules.organizations.application.dto.VerificationDecision;
import com.projeto.ticket_wave.modules.organizations.application.dto.VerificationSubmission;
import com.projeto.ticket_wave.modules.organizations.application.dto.VerificationSummary;
import com.projeto.ticket_wave.modules.organizations.application.service.OrganizationVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/organizations")
@RequiredArgsConstructor
public class OrganizationVerificationController {
    private final OrganizationVerificationService verification;

    @PostMapping("/{id}/verification/submit")
    public ResponseEntity<VerificationSummary> submit(@PathVariable UUID id, @Valid @RequestBody VerificationSubmission request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(verification.submit(id, request));
    }

    @PatchMapping("/{id}/verification/review")
    public ResponseEntity<VerificationSummary> review(@PathVariable UUID id, @Valid @RequestBody VerificationDecision request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(verification.review(id, request));
    }

    @GetMapping("/verification/queue")
    public ResponseEntity<Page<VerificationSummary>> queue(Pageable pageable) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(verification.queue(pageable));
    }
}
