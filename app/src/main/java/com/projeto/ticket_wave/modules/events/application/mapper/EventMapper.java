package com.projeto.ticket_wave.modules.events.application.mapper;

import com.projeto.ticket_wave.modules.events.application.dto.CreateEventRequest;
import com.projeto.ticket_wave.modules.events.application.dto.EventResponse;
import com.projeto.ticket_wave.modules.events.application.dto.EventSummaryResponse;
import com.projeto.ticket_wave.modules.events.application.dto.UpdateEventRequest;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventMapper {
    Event toEntity(CreateEventRequest dto);
    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "venue", source = "venue.name")
    @Mapping(target = "organizer", source = "organizer.name")
    @Mapping(target = "eventStatus", source = "eventStatus")
    @Mapping(target = "eventType", source = "eventType")
    EventResponse toResponse(Event event);
    List<EventResponse> toResponseList(List<Event> events);

    @Mapping(target = "city", source = "venue.address.city")
    @Mapping(target = "status", source = "eventStatus")
    EventSummaryResponse toSummary(Event event);
    List<EventSummaryResponse> toSummaryList(List<Event> events);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEvent(UpdateEventRequest dto, @MappingTarget Event event);
}
