package com.projeto.ticket_wave.domain.search;

import com.projeto.ticket_wave.domain.entity.Event;
import com.projeto.ticket_wave.domain.enums.EventSort;
import com.projeto.ticket_wave.domain.specification.EventSpecification;
import com.projeto.ticket_wave.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class JpaEventSearchEngine implements EventSearchEngine {

    private final EventRepository eventRepository;

    public JpaEventSearchEngine(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Page<Event> search(EventSearchCriteria criteria, Pageable pageable) {
        Sort sort = resolveSort(criteria.sort());
        Pageable effectivePageable = sort.isUnsorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return eventRepository.findAll(EventSpecification.withFilters(criteria), effectivePageable);
    }

    private Sort resolveSort(EventSort sort) {
        if (sort == null) return Sort.unsorted();

        return switch (sort.name()) {
            case "PRICE_ASC" -> Sort.by(Sort.Direction.ASC, "ticketPrice");
            case "PRICE_DESC" -> Sort.by(Sort.Direction.DESC, "ticketPrice");
            case "DATE_ASC" -> Sort.by(Sort.Direction.ASC, "startDate");
            case "DATE_DESC" -> Sort.by(Sort.Direction.DESC, "startDate");
            case "NEWEST" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.unsorted();
        };
    }

