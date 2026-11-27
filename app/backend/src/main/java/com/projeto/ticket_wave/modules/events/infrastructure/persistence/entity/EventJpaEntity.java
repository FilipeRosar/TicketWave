package com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.events.domain.enums.EventClassification;
import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Event")
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_event_slug", columnList = "slug"),
                @Index(name = "idx_event_status", columnList = "eventStatus"),
                @Index(name = "idx_event_start", columnList = "startDate"),
                @Index(name = "idx_event_category", columnList = "category_id"),
                @Index(name = "idx_event_organizer", columnList = "organizer_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class EventJpaEntity extends JpaBaseEntity {
    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 180, unique = true)
    private String slug;

    @Column(nullable = false, length = 550)
    private String shortDescription;

    @Column(nullable = false, name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer availableTickets;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus eventStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventClassification classification = EventClassification.SOCIAL;

    @Column(nullable = false)
    private Boolean published =  false;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Boolean aiAssistantEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private UserJpaEntity publishedBy;

    @Column(nullable = false)
    private Boolean networkingEnabled = true;

    @Column(nullable = false)
    private Boolean gamificationEnabled = true;

    private String streamingUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryJpaEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private UserJpaEntity organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private VenueJpaEntity venue;

    @Column(nullable = false)
    private Integer totalViews;

    @Column(nullable = false)
    private Integer totalTicketsSold;

    @Column(nullable = false)
    private Integer totalFavorites;

    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EventImageJpaEntity> images = new ArrayList<>();
}
