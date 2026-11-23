package com.projeto.ticket_wave.modules.events.domain.entity;

import com.projeto.ticket_wave.modules.events.domain.enums.EventStatus;
import com.projeto.ticket_wave.modules.events.domain.enums.EventType;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.shared.exception.BusinessException;
import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event extends BaseEntity {
    private String title;

    private String slug;

    private String shortDescription;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer capacity;

    private Integer availableTickets;

    private BigDecimal ticketPrice;

    private EventStatus eventStatus;

    private EventType eventType;

    @Builder.Default
    private Boolean published =  false;

    @Builder.Default
    private Boolean featured = false;

    @Builder.Default
    private Boolean aiAssistantEnabled = true;

    private User publishedBy;

    @Builder.Default
    private Boolean networkingEnabled = true;

    @Builder.Default
    private Boolean gamificationEnabled = true;

    private String streamingUrl;

    private Category category;

    private User organizer;

    private Venue venue;

    private Integer totalViews;

    private Integer totalTicketsSold;

    private Integer totalFavorites;

    @Builder.Default
    private List<EventImage> images = new ArrayList<>();

    public void validateDates(){
        if (!endDate.isAfter(startDate)) {
            throw new BusinessException("A data de término deve ser posterior à data de início.");
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
