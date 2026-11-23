package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :id")
    Optional<UserJpaEntity> findForSessionUpdate(@Param("id") UUID id);

    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
