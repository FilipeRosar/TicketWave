package com.projeto.ticket_wave.modules.ticketing.domain.entity;

import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.ticketing.domain.enums.TicketStatus;
import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket extends BaseEntity {
    private Order order;

    private TicketBatch batch;

    private User holder;

    private String qrCode;

    private TicketStatus status;

    private Instant usedAt;
}
