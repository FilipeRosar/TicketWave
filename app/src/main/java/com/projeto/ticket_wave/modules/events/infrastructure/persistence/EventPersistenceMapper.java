package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.domain.entity.Address;
import com.projeto.ticket_wave.modules.events.domain.entity.Category;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.entity.EventImage;
import com.projeto.ticket_wave.modules.events.domain.entity.Venue;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.AddressJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.CategoryJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventImageJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.VenueJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.IdentityPersistenceMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = IdentityPersistenceMapper.class)
public interface EventPersistenceMapper {
    @Mapping(target = "images", ignore = true)
    Event toDomain(EventJpaEntity source);

    default Event toDomainWithImages(EventJpaEntity source) {
        Event event = toDomain(source);
        for (EventImageJpaEntity image : source.getImages()) {
            EventImage domainImage = toDomain(image);
            domainImage.setEvent(event);
            event.getImages().add(domainImage);
        }
        return event;
    }

    @Mapping(target = "event", qualifiedByName = "eventReference")
    EventImage toDomain(EventImageJpaEntity source);

    Category toDomain(CategoryJpaEntity source);

    Venue toDomain(VenueJpaEntity source);

    Address toDomain(AddressJpaEntity source);

    @Named("eventReference")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Event toEventReference(EventJpaEntity source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "publishedBy", ignore = true)
    @Mapping(target = "venue", ignore = true)
    void copyToJpa(Event source, @MappingTarget EventJpaEntity target);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "event", ignore = true)
    void copyToJpa(EventImage source, @MappingTarget EventImageJpaEntity target);
}
