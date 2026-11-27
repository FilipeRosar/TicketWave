package com.projeto.ticket_wave.modules.events.application.service;

import com.projeto.ticket_wave.modules.events.application.dto.CreateEventRequest;
import com.projeto.ticket_wave.modules.events.application.dto.EventResponse;
import com.projeto.ticket_wave.modules.events.application.dto.EventSummaryResponse;
import com.projeto.ticket_wave.modules.events.application.dto.UpdateEventRequest;
import com.projeto.ticket_wave.modules.events.application.mapper.EventMapper;
import com.projeto.ticket_wave.modules.events.domain.entity.Category;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.entity.Venue;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.search.EventSearchCriteria;
import com.projeto.ticket_wave.modules.events.application.port.CategoryRepository;
import com.projeto.ticket_wave.modules.events.application.port.EventRepository;
import com.projeto.ticket_wave.modules.events.application.port.VenueRepository;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.application.port.CurrentUser;
import com.projeto.ticket_wave.shared.exception.ForbiddenException;
import com.projeto.ticket_wave.shared.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    private final EventRepository eventRepository;

    private final CategoryRepository  categoryRepository;

    private final VenueRepository venueRepository;

    private final EventMapper eventMapper;

    private final SlugService slugService;

    private final CurrentUser currentUser;

    public EventResponse createEvent(CreateEventRequest request){
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new NotFoundException("Local não encontrado"));

        User organizer = currentUser.getCurrentUser();

        Event event = eventMapper.toEntity(request);

        event.setCategory(category);

        event.setVenue(venue);

        event.setOrganizer(organizer);

        event.setSlug(slugService.generate(request.title()));

        event.setPublished(false);

        event.setFeatured(false);

        event.setEventStatus(EventStatus.DRAFT);

        event.setAvailableTickets(request.capacity());
        event.setTotalViews(0);
        event.setTotalTicketsSold(0);
        event.setTotalFavorites(0);
        event.validateDates();

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest request){
        Event event = findOwnedEvent(id);

        event.requireEditable();
        int previousCapacity = event.getCapacity();
        eventMapper.updateEvent(request, event);
        event.validateDates();
        if (request.capacity() != null) event.updateCapacity(previousCapacity);

        if(request.categoryId() != null){
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
            event.setCategory(category);
        }
        if(request.venueId() != null){
            Venue venue = venueRepository.findById(request.venueId())
                    .orElseThrow(() -> new NotFoundException("Local não encontrado"));
            event.setVenue(venue);
        }
        return eventMapper.toResponse(eventRepository.save(event));
    }
    public EventResponse publishEvent(UUID id) {
        Event event = findOwnedEvent(id);
        if (event.isPublishedInCatalog()) return eventMapper.toResponse(event);
        event.publish(LocalDateTime.now());
        return eventMapper.toResponse(eventRepository.save(event));
    }

    public EventResponse cancelEvent(UUID id) {
        Event event = findOwnedEvent(id);
        if (event.getEventStatus() == EventStatus.CANCELLED) return eventMapper.toResponse(event);
        event.cancel();
        return eventMapper.toResponse(eventRepository.save(event));
    }

    public EventResponse findVisibleById(UUID id) {
        Event event = findEntity(id);
        if (!event.isPublishedInCatalog()
                && !event.getOrganizer().getId().equals(currentUser.getCurrentUser().getId())) {
            throw new NotFoundException("Evento não encontrado");
        }
        return eventMapper.toResponse(event);
    }

    public EventResponse findById(UUID id){
        return eventMapper.toResponse(findEntity(id));
    }
    @Transactional
    public EventResponse findBySlug(String slug){
        Event event = eventRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new NotFoundException("Evento não encontrado"));
        return eventMapper.toResponse(event);
    }
    @Transactional
    public Page<EventSummaryResponse> findAll(Pageable pageable){
        return eventRepository
                .findPublished(pageable)
                .map(eventMapper::toSummary);
    }

    @Transactional
    public void deleteEvent(UUID id){
        Event event = findOwnedEvent(id);
        event.requireDeletable();
        eventRepository.delete(event);
    }

    public Page<EventSummaryResponse> search(EventSearchCriteria search, Pageable pageable){
        return eventRepository.search(search, pageable)
                .map(eventMapper::toSummary);
    }

    public Page<EventSummaryResponse> findMyEvents(Pageable pageable) {
        return eventRepository.findByOrganizer(currentUser.getCurrentUser().getId(), pageable)
                .map(eventMapper::toSummary);
    }

    public Page<EventSummaryResponse> searchPublished(EventSearchCriteria search, Pageable pageable) {
        return eventRepository.searchPublished(search, pageable).map(eventMapper::toSummary);
    }

    private Event findOwnedEvent(UUID id) {
        Event event = findEntity(id);
        if (!event.getOrganizer().getId().equals(currentUser.getCurrentUser().getId())) {
            throw new ForbiddenException("Somente o organizador responsável pode gerenciar este evento.");
        }
        return event;
    }

    private Event findEntity(UUID id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Evento não encontrado"));
    }

}
