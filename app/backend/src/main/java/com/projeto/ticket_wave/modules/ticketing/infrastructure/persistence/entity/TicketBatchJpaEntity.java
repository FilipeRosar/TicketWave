package com.projeto.ticket_wave.modules.ticketing.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity(name = "TicketBatch")
@Table(name = "ticket_batches")
@Getter
@Setter
@NoArgsConstructor
public class TicketBatchJpaEntity extends JpaBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventJpaEntity event;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Instant salesStart;

    @Column(nullable = false)
    private Instant salesEnd;
}
