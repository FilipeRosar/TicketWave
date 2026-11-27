package com.projeto.ticket_wave.modules.identity.application.port;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository {
    boolean issue(UUID userId, String tokenHash, long validitySeconds);
    Optional<UUID> consume(String tokenHash);
}
