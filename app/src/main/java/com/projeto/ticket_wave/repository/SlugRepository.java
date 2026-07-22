package com.projeto.ticket_wave.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface SlugRepository {
    boolean existsBySlug(String slug);
}
