package com.projeto.ticket_wave.modules.events.adapters.rest;

import com.projeto.ticket_wave.modules.events.application.dto.CreateEventRequest;
import com.projeto.ticket_wave.modules.events.application.dto.EventResponse;
import com.projeto.ticket_wave.modules.events.application.dto.EventSummaryResponse;
import com.projeto.ticket_wave.modules.events.application.dto.UpdateEventRequest;
import com.projeto.ticket_wave.modules.events.application.service.EventService;
import com.projeto.ticket_wave.modules.events.domain.search.EventSearchCriteria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EventResponse>  createEvent(@Valid @RequestBody CreateEventRequest createEventRequest){
        return ResponseEntity.ok(eventService.createEvent(createEventRequest));
    }
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id){
        return ResponseEntity.ok(eventService.findVisibleById(id));
    }
    @GetMapping
    public ResponseEntity<Page<EventSummaryResponse>> getAllEvents(Pageable pageable){
        return ResponseEntity.ok(eventService.findAll(pageable));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<EventSummaryResponse>> search(EventSearchCriteria search, Pageable pageable){
        return ResponseEntity.ok(eventService.searchPublished(search, pageable));
    }
    @GetMapping("/mine")
    public Page<EventSummaryResponse> myEvents(@RequestParam(required = false) UUID organizationId, Pageable pageable) {
        return eventService.findMyEvents(organizationId, pageable);
    }

    @PatchMapping("/{id}/publish")
    public EventResponse publishEvent(@PathVariable UUID id) {
        return eventService.publishEvent(id);
    }

    @PatchMapping("/{id}/cancel")
    public EventResponse cancelEvent(@PathVariable UUID id) {
        return eventService.cancelEvent(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id,@Valid @RequestBody UpdateEventRequest dto){
        return ResponseEntity.ok(eventService.updateEvent(id,dto));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id){
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

}
