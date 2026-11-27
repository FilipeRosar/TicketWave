package com.projeto.ticket_wave.modules.organizations.adapters.rest;

import com.projeto.ticket_wave.modules.organizations.application.dto.OrganizationMemberResponse;
import com.projeto.ticket_wave.modules.organizations.application.dto.UpdateOrganizationMemberRequest;
import com.projeto.ticket_wave.modules.organizations.application.service.OrganizationMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/organizations/{organizationId}/members")
@RequiredArgsConstructor
public class OrganizationMemberController {
    private final OrganizationMembershipService members;

    @GetMapping
    public ResponseEntity<List<OrganizationMemberResponse>> list(@PathVariable UUID organizationId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(members.list(organizationId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<OrganizationMemberResponse> assign(@PathVariable UUID organizationId, @PathVariable UUID userId,
                                                            @Valid @RequestBody UpdateOrganizationMemberRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(members.assign(organizationId, userId, request.role()));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID organizationId, @PathVariable UUID userId) {
        members.remove(organizationId, userId);
    }
}
