package com.projeto.ticket_wave.modules.ticketing.domain.entity;

import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketBatch extends BaseEntity {
    private Event event;

    private String name;

    private BigDecimal price;

    private int quantity;

    private Instant salesStart;

    private Instant salesEnd;
}
