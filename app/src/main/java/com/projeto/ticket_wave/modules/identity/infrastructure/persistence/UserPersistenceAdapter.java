package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.application.port.UserRepository;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.shared.persistence.JpaEntityAccess;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class UserPersistenceAdapter implements UserRepository {
    private final UserJpaRepository repository;
    private final IdentityPersistenceMapper mapper;
    private final JpaEntityAccess entities;

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = entities.forWrite(user, UserJpaEntity.class, UserJpaEntity::new);
        mapper.copyToJpa(user, entity);
        return mapper.toDomain(repository.saveAndFlush(entity));
    }
}
