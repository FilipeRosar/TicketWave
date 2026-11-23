package com.projeto.ticket_wave.modules.identity.application.port;

import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);
}
