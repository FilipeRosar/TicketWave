package com.projeto.ticket_wave.modules.identity.application.port;

import com.projeto.ticket_wave.modules.identity.domain.entity.RefreshToken;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void lockUserSession(UUID userId);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken save(RefreshToken token);

    void delete(RefreshToken token);

    void deleteByUser(User user);
}
