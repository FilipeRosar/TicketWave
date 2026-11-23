package com.projeto.ticket_wave.modules.ticketing.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.modules.ticketing.domain.enums.TicketStatus;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity(name = "Ticket")
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class TicketJpaEntity extends JpaBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private TicketBatchJpaEntity batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "holder_id", nullable = false)
    private UserJpaEntity holder;

    @Column(nullable = false, unique = true, length = 128)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status;

    private Instant usedAt;
}
