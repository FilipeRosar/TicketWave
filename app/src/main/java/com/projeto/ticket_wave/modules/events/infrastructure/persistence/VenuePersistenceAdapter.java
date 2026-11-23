package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.application.port.VenueRepository;
import com.projeto.ticket_wave.modules.events.domain.entity.Venue;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class VenuePersistenceAdapter implements VenueRepository {
    private final VenueJpaRepository repository;
    private final EventPersistenceMapper mapper;

    @Override
    public Optional<Venue> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
