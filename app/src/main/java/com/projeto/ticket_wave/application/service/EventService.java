package com.projeto.ticket_wave.application.service;

import com.projeto.ticket_wave.application.dto.EventDTOs.*;
import com.projeto.ticket_wave.application.mapper.EventMapper;
import com.projeto.ticket_wave.domain.entity.Category;
import com.projeto.ticket_wave.domain.entity.Event;
import com.projeto.ticket_wave.domain.entity.User;
import com.projeto.ticket_wave.domain.entity.Venue;
import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.search.EventSearchCriteria;
import com.projeto.ticket_wave.domain.specification.EventSpecification;
import com.projeto.ticket_wave.infrastructure.exception.NotFoundException;
import com.projeto.ticket_wave.infrastructure.security.AuthenticationFacade;
import com.projeto.ticket_wave.infrastructure.security.CustomUserDetails;
import com.projeto.ticket_wave.repository.CategoryRepository;
import com.projeto.ticket_wave.repository.EventRepository;
import com.projeto.ticket_wave.repository.UserRepository;
import com.projeto.ticket_wave.repository.VenueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {
    private final EventRepository eventRepository;

    private final CategoryRepository  categoryRepository;

    private final VenueRepository venueRepository;

    private final UserRepository userRepository;

    private final EventMapper eventMapper;

    private final SlugService slugService;

    private final AuthenticationFacade authenticationFacade;

    public EventResponse createEvent(CreateEventRequest request){
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new NotFoundException("Local não encontrado"));

        User organizer = authenticationFacade.getCurrentUser();

        Event event = eventMapper.toEntity(request);

        event.setCategory(category);

        event.setVenue(venue);

        event.setOrganizer(organizer);

        event.setSlug(slugService.generate(request.title()));

        event.setPublished(false);

        event.setFeatured(false);

        event.setEventStatus(EventStatus.DRAFT);

        event.setAvailableTickets(request.capacity());

        eventRepository.save(event);

        return eventMapper.toResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest request){
        Event event = findEntity(id);

        eventMapper.updateEvent(request, event);

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
        Event event = findEntity(id);
        eventRepository.delete(event);
    }

    public Page<EventSummaryResponse> search(EventSearchCriteria search, Pageable pageable){
        return eventRepository.findAll(EventSpecification.withFilters(search), pageable)
                .map(eventMapper::toSummary);
    }

    private Event findEntity(UUID id){
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Evento não encontrado"));
    }

}
