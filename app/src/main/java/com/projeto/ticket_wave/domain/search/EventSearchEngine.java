package com.projeto.ticket_wave.domain.search;

import com.projeto.ticket_wave.domain.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventSearchEngine {
    Page<Event> search(EventSearchCriteria criteria, Pageable pageable);
}
