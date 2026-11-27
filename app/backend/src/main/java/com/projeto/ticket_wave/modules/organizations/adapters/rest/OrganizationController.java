package com.projeto.ticket_wave.modules.organizations.adapters.rest;

import com.projeto.ticket_wave.modules.organizations.application.dto.CreateOrganizationRequest;
import com.projeto.ticket_wave.modules.organizations.application.dto.OrganizationResponse;
import com.projeto.ticket_wave.modules.organizations.application.service.OrganizationRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationRegistrationService registration;

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(registration.create(request));
    }
}
