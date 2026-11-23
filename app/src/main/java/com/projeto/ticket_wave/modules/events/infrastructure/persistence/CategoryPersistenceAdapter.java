package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.application.port.CategoryRepository;
import com.projeto.ticket_wave.modules.events.domain.entity.Category;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CategoryPersistenceAdapter implements CategoryRepository {
    private final CategoryJpaRepository repository;
    private final EventPersistenceMapper mapper;

    @Override
    public Optional<Category> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
