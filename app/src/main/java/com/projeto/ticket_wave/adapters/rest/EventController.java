package com.projeto.ticket_wave.adapters.rest;

import com.projeto.ticket_wave.application.dto.EventDTOs.*;
import com.projeto.ticket_wave.application.service.EventService;
import com.projeto.ticket_wave.domain.search.EventSearchCriteria;
import com.sun.jdi.request.EventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/api/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<EventResponse>  createEvent(@Valid @RequestBody CreateEventRequest createEventRequest){
        return ResponseEntity.ok(eventService.createEvent(createEventRequest));
    }
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable UUID id){
        return ResponseEntity.ok(eventService.findById(id));
    }
    @GetMapping
    public ResponseEntity<Page<EventSummaryResponse>> getAllEvents(Pageable pageable){
        return ResponseEntity.ok(eventService.findAll(pageable));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<EventSummaryResponse>> search(EventSearchCriteria search, Pageable pageable){
        return ResponseEntity.ok(eventService.search(search,pageable));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable UUID id,@Valid @RequestBody UpdateEventRequest dto){
        return ResponseEntity.ok(eventService.updateEvent(id,dto));
    }
    @DeleteMapping
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id){
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

}
