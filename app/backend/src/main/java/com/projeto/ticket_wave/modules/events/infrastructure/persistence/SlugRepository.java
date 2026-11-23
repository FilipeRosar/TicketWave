package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import org.springframework.stereotype.Repository;

@Repository
public interface SlugRepository {
    boolean existsBySlug(String slug);
}
