package com.projeto.ticket_wave.domain.entity;


import com.projeto.ticket_wave.domain.enums.EventStatus;
import com.projeto.ticket_wave.domain.enums.EventType;
import com.projeto.ticket_wave.infrastructure.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {
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

    @Builder.Default
    @Column(nullable = false)
    private Boolean published =  false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean featured = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean aiAssistantEnabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Builder.Default
    @Column(nullable = false)
    private Boolean networkingEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean gamificationEnabled = true;

    private String streamingUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(nullable = false)
    private Integer totalViews;

    @Column(nullable = false)
    private Integer totalTicketsSold;

    @Column(nullable = false)
    private Integer totalFavorites;

    @Builder.Default
    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<EventImage> images = new ArrayList<>();

    public void validateDates(){
        if(endDate.isBefore(startDate)){
            throw new BusinessException("End date cannot be before start date");
        }
    }
    public void validateCapacity(){
        if(capacity <= 0){
            throw new BusinessException("Capacity cannot be less than 0");
        }
    }
    public void validatePrice(){
        if(ticketPrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("Price cannot be less than 0");
        }
    }

}
