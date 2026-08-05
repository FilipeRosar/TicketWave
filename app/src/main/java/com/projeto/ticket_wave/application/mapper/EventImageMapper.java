package com.projeto.ticket_wave.application.mapper;

import com.projeto.ticket_wave.application.dto.EventImageDTOs.ImageResponse;
import com.projeto.ticket_wave.domain.entity.EventImage;
import org.springframework.stereotype.Component;

@Component
public class EventImageMapper {
    public ImageResponse toDto(EventImage eventImage) {
        return new ImageResponse(
                eventImage.getId(),
                eventImage.getImageUrl(),
                eventImage.getCover(),
                eventImage.getDisplayOrder()
        );
    }
}
