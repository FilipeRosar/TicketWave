package com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity;

import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "EventImage")
@Table(name = "event_image")
@Getter
@Setter
@NoArgsConstructor
public class EventImageJpaEntity extends JpaBaseEntity {
    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String imageKey;

    @Column(nullable = false)
    private Boolean cover;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private EventJpaEntity event;
}
