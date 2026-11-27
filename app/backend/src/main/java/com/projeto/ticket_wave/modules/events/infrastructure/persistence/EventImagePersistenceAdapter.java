package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.application.port.EventImageRepository;
import com.projeto.ticket_wave.modules.events.domain.entity.EventImage;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventImageJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.shared.persistence.JpaEntityAccess;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole.EVENT_MANAGERS;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class EventImagePersistenceAdapter implements EventImageRepository {
    private final EventImageJpaRepository repository;
    private final EventPersistenceMapper mapper;
    private final JpaEntityAccess entities;

    @Override
    public Optional<EventImage> findManagedImage(UUID id, UUID userId) {
        return repository.findManagedImage(id, userId, EVENT_MANAGERS).map(mapper::toDomain);
    }

    @Override
    public List<EventImage> findByEventId(UUID eventId) {
        return repository.findByEventId(eventId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<EventImage> findByEventIdOrderByDisplayOrder(UUID eventId) {
        return repository.findByEventIdOrderByDisplayOrder(eventId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public EventImage save(EventImage image) {
        return mapper.toDomain(repository.saveAndFlush(toJpa(image)));
    }

    @Override
    @Transactional
    public void saveAll(List<EventImage> images) {
        repository.saveAll(images.stream().map(this::toJpa).toList());
    }

    @Override
    @Transactional
    public void delete(EventImage image) {
        repository.delete(entities.existing(image, EventImageJpaEntity.class));
    }

    private EventImageJpaEntity toJpa(EventImage image) {
        EventImageJpaEntity entity = entities.forWrite(image, EventImageJpaEntity.class, EventImageJpaEntity::new);
        mapper.copyToJpa(image, entity);
        entity.setEvent(entities.reference(image.getEvent(), EventJpaEntity.class));
        return entity;
    }
}
