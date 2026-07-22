package com.projeto.ticket_wave.application.mapper;

import com.projeto.ticket_wave.application.dto.EventDTOs.CreateEventRequest;
import com.projeto.ticket_wave.application.dto.EventDTOs.EventResponse;
import com.projeto.ticket_wave.application.dto.EventDTOs.EventSummaryResponse;
import com.projeto.ticket_wave.application.dto.EventDTOs.UpdateEventRequest;
import com.projeto.ticket_wave.domain.entity.Event;
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

    EventSummaryResponse toSummary(Event event);
    List<EventSummaryResponse> toSummaryList(List<Event> events);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEvent(UpdateEventRequest dto, @MappingTarget Event event);
}
