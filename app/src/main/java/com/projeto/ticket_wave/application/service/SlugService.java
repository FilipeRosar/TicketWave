package com.projeto.ticket_wave.application.service;


import com.projeto.ticket_wave.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SlugService {
    private final EventRepository eventRepository;

    public String generate(String text) {
        String baseSlug = slugify(text);
        String slug = baseSlug;

        int count = 1;
        while (eventRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count++;
        }
        return slug;
    }
    public String slugify(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized
                .replace("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

    }
}
