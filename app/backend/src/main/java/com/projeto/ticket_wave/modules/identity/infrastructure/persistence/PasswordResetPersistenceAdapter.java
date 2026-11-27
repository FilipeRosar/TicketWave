package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.application.port.PasswordResetRepository;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
class PasswordResetPersistenceAdapter implements PasswordResetRepository {
    private static final long REISSUE_DELAY_SECONDS = 60;

    private final PasswordResetTokenJpaRepository tokens;
    private final UserJpaRepository users;

    @Override
    public boolean issue(UUID userId, String tokenHash, long validitySeconds) {
        if (!lockEnabledUser(userId)) return false;

        var token = tokens.findById(userId).orElseGet(() -> new PasswordResetTokenJpaEntity(userId));
        Instant now = Instant.now();
        if (token.getExpiresAt() != null && token.getExpiresAt().isAfter(now)
                && token.getIssuedAt().plusSeconds(REISSUE_DELAY_SECONDS).isAfter(now)) {
            return false;
        }
        token.issue(tokenHash, now, now.plusSeconds(validitySeconds));
        tokens.save(token);
        return true;
    }

    @Override
    public Optional<UUID> consume(String tokenHash) {
        var target = tokens.findUserIdByHash(tokenHash);
        if (target.isEmpty() || !lockEnabledUser(target.get())) return Optional.empty();

        return tokens.findById(target.get())
                .filter(token -> token.getTokenHash().equals(tokenHash) && token.getExpiresAt().isAfter(Instant.now()))
                .map(token -> {
                    tokens.delete(token);
                    return token.getUserId();
                });
    }

    private boolean lockEnabledUser(UUID userId) {
        return users.findForSessionUpdate(userId).map(UserJpaEntity::getEnabled).orElse(false);
    }
}
