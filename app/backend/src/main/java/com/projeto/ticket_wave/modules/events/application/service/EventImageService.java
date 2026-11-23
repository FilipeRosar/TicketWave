package com.projeto.ticket_wave.modules.events.application.service;

import com.projeto.ticket_wave.modules.events.application.dto.ImageResponse;
import com.projeto.ticket_wave.modules.events.application.mapper.EventImageMapper;
import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.events.domain.entity.EventImage;
import com.projeto.ticket_wave.modules.events.application.port.EventImageRepository;
import com.projeto.ticket_wave.modules.events.application.port.EventRepository;
import com.projeto.ticket_wave.shared.exception.NotFoundException;
import com.projeto.ticket_wave.shared.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventImageService {
    private final StorageService storageService;
    private final EventRepository  eventRepository;
    private final EventImageRepository eventImageRepository;
    private final EventImageMapper mapper;

    public ImageResponse upload(UUID eventId, MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Evento não encontrado"));

        String imageUrl = storageService.upload(file,"events");

        int imageCount = event.getImages().size();
        EventImage eventImage = EventImage.builder()
                .event(event)
                .cover(imageCount == 0)
                .displayOrder(imageCount + 1)
                .build();
        return mapper.toDto(eventImageRepository.save(eventImage));
    }
    public List<ImageResponse> findByEventId(UUID eventId) {
        return eventImageRepository
                .findByEventIdOrderByDisplayOrder(eventId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
    @Transactional
    public ImageResponse defineCover(UUID imageId) {
        EventImage image = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada"));

        List<EventImage> images = eventImageRepository.findByEventId(image.getEvent().getId());
        images.forEach(candidate -> candidate.setCover(candidate.getId().equals(imageId)));
        eventImageRepository.saveAll(images);
        image.setCover(true);
        return mapper.toDto(image);
    }
    public void delete(UUID imageId) {
        EventImage image = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada"));

        storageService.delete(extractKey(image.getImageUrl()));

        eventImageRepository.delete(image);
    }

    private String extractKey(String url) {
        String bucket = "/ticketwave-images";
        return url.substring(
                url.indexOf(bucket)+bucket.length()
        );
    }

}
