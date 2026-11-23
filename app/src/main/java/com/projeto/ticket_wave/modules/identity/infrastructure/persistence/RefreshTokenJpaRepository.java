package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByToken(String token);
    void  deleteByUser(UserJpaEntity user);
    void delete(RefreshTokenJpaEntity refreshToken);
}
