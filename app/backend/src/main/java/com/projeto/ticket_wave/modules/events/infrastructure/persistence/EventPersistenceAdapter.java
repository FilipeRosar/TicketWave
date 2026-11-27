package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.application.dto.EventReportRow;
import com.projeto.ticket_wave.modules.events.application.port.EventRepository;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.enums.EventSort;
import com.projeto.ticket_wave.modules.events.domain.search.EventSearchCriteria;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.CategoryJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.VenueJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission;
import com.projeto.ticket_wave.shared.persistence.JpaEntityAccess;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission.READ_EVENTS;
import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationPermission.READ_REPORTS;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class EventPersistenceAdapter implements EventRepository {
    private final EventJpaRepository repository;
    private final EventPersistenceMapper mapper;
    private final JpaEntityAccess entities;

    @Override
    public Optional<Event> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomainWithImages);
    }

    @Override
    public Optional<Event> findManagedEvent(UUID id, UUID userId, OrganizationPermission permission) {
        return repository.findManagedEvent(id, userId, permission.roles()).map(mapper::toDomainWithImages);
    }

    @Override
    public Optional<Event> findVisibleEvent(UUID id, UUID userId) {
        return repository.findVisibleEvent(id, userId, READ_EVENTS.roles()).map(mapper::toDomainWithImages);
    }

    @Override
    public List<EventReportRow> report(UUID organizationId, UUID userId) {
        return repository.report(organizationId, userId, READ_REPORTS.roles());
    }

    @Override
    public Optional<Event> findBySlugIgnoreCase(String slug) {
        return repository.findBySlugIgnoreCase(slug).map(mapper::toDomainWithImages);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public Page<Event> findPublished(Pageable pageable) {
        return repository.findPublished(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Event> findManageableByMember(UUID userId, UUID organizationId, Pageable pageable) {
        return repository.findManageableByMember(userId, organizationId, READ_EVENTS.roles(), pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Event> searchPublished(EventSearchCriteria criteria, Pageable pageable) {
        return search(EventSpecification.published().and(EventSpecification.withFilters(criteria)), criteria.sort(), pageable);
    }

    @Override
    public Page<Event> search(EventSearchCriteria criteria, Pageable pageable) {
        return search(EventSpecification.withFilters(criteria), criteria.sort(), pageable);
    }

    private Page<Event> search(Specification<EventJpaEntity> specification, EventSort order, Pageable pageable) {
        Sort sort = searchSort(order);
        Pageable page = pageable;
        if (sort.isSorted()) {
            sort = sort.and(Sort.by("id"));
            page = pageable.isPaged()
                    ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                    : Pageable.unpaged(sort);
        }
        return repository.findAll(specification, page).map(mapper::toDomain);
    }

    private Sort searchSort(EventSort sort) {
        if (sort == null) {
            return Sort.unsorted();
        }
        return switch (sort) {
            case DATE_ASC -> Sort.by("startDate");
            case DATE_DESC -> Sort.by(Sort.Direction.DESC, "startDate");
            case PRICE_ASC -> Sort.by("ticketPrice");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "ticketPrice");
            case POPULARITY -> Sort.by(Sort.Direction.DESC, "totalViews");
            case FEATUREDs -> Sort.by(Sort.Direction.DESC, "featured");
            case RELEVANCE -> Sort.unsorted();
        };
    }

    @Override
    @Transactional
    public Event save(Event event) {
        EventJpaEntity entity = entities.forWrite(event, EventJpaEntity.class, EventJpaEntity::new);
        mapper.copyToJpa(event, entity);
        entity.setCategory(entities.reference(event.getCategory(), CategoryJpaEntity.class));
        entity.setVenue(entities.reference(event.getVenue(), VenueJpaEntity.class));
        entity.setOrganizer(entities.reference(event.getOrganizer(), UserJpaEntity.class));
        entity.setPublishedBy(entities.reference(event.getPublishedBy(), UserJpaEntity.class));
        return mapper.toDomainWithImages(repository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public void delete(Event event) {
        repository.delete(entities.existing(event, EventJpaEntity.class));
    }
}
