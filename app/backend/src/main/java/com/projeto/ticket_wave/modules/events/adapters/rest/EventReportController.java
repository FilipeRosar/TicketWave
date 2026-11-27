package com.projeto.ticket_wave.modules.events.adapters.rest;

import com.projeto.ticket_wave.modules.events.application.dto.EventReport;
import com.projeto.ticket_wave.modules.events.application.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/organizations/{organizationId}/reports/events")
@RequiredArgsConstructor
public class EventReportController {
    private final EventService events;

    @GetMapping
    public ResponseEntity<EventReport> eventReport(@PathVariable UUID organizationId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(events.report(organizationId));
    }
}
