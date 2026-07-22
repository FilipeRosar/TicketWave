package com.projeto.ticket_wave.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_image")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventImage extends BaseEntity {
    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String imageKey;

    @Column(nullable = false)
    private Boolean cover;

    @Column(nullable = false)
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}
