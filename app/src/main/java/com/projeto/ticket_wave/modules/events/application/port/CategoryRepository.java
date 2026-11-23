package com.projeto.ticket_wave.modules.events.application.port;

import com.projeto.ticket_wave.modules.events.domain.entity.Category;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Optional<Category> findById(UUID id);
}
