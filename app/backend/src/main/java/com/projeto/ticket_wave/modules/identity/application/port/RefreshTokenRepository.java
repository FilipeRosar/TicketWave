package com.projeto.ticket_wave.modules.identity.application.port;

import com.projeto.ticket_wave.modules.identity.domain.entity.RefreshToken;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken save(RefreshToken token);

    void delete(RefreshToken token);

    void deleteByUser(User user);
}
