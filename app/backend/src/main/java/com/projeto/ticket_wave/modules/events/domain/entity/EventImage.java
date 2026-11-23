package com.projeto.ticket_wave.modules.events.domain.entity;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventImage extends BaseEntity {
    private String imageUrl;

    private String imageKey;

    private Boolean cover;

    private Integer displayOrder;

    private Integer width;

    private Integer height;

    private Long size;

    private String fileName;

    private String contentType;

    private Event event;
}
