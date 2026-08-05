package com.projeto.ticket_wave.application.service;

import com.projeto.ticket_wave.application.dto.EventImageDTOs.ImageResponse;
import com.projeto.ticket_wave.application.interfaces.StorageService;
import com.projeto.ticket_wave.application.mapper.EventImageMapper;
import com.projeto.ticket_wave.domain.entity.Event;
import com.projeto.ticket_wave.domain.entity.EventImage;
import com.projeto.ticket_wave.infrastructure.exception.NotFoundException;
import com.projeto.ticket_wave.repository.EventImageRepository;
import com.projeto.ticket_wave.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

        EventImage eventImage = EventImage.builder()
                .event(event)
                .cover(event.getImages().isEmpty())
                .displayOrder(event.getImages().size()+1)
                .build();
        eventImageRepository.save(eventImage);
        return mapper.toDto(eventImage);
    }
    public List<ImageResponse> findByEventId(UUID eventId) {
        return eventImageRepository
                .findByEventIdOrderByDisplayOrder(eventId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }
    public ImageResponse defineCover(UUID imageId) {
        EventImage image = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada"));

        eventImageRepository.findByEventId(image.getEvent().getId())
                .forEach(i -> i.setCover(false));
        image.setCover(true);

        eventImageRepository.saveAll(
                eventImageRepository.findByEventId(image.getEvent().getId())
        );
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
