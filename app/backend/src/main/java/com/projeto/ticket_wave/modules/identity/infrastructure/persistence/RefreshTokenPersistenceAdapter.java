package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.application.port.RefreshTokenRepository;
import com.projeto.ticket_wave.modules.identity.domain.entity.RefreshToken;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.shared.persistence.JpaEntityAccess;
import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository repository;
    private final UserJpaRepository users;
    private final IdentityPersistenceMapper mapper;
    private final JpaEntityAccess entities;

    @Override
    @Transactional
    public void lockUserSession(UUID userId) {
        UserJpaEntity user = users.findForSessionUpdate(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário indisponível"));
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("Usuário indisponível");
        }
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity entity = entities.forWrite(token, RefreshTokenJpaEntity.class, RefreshTokenJpaEntity::new);
        mapper.copyToJpa(token, entity);
        entity.setUser(entities.reference(token.getUser(), UserJpaEntity.class));
        return mapper.toDomain(repository.saveAndFlush(entity));
    }

    @Override
    @Transactional
    public void delete(RefreshToken token) {
        repository.delete(entities.existing(token, RefreshTokenJpaEntity.class));
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        repository.deleteByUser(entities.reference(user, UserJpaEntity.class));
    }
}
