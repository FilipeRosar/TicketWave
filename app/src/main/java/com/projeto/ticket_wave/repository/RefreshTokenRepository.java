package com.projeto.ticket_wave.repository;

import com.projeto.ticket_wave.domain.entity.RefreshToken;
import com.projeto.ticket_wave.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void  deleteByUser(User user);
    void delete(RefreshToken refreshToken);
}
