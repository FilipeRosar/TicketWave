package com.projeto.ticket_wave.modules.ticketing.domain.entity;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends BaseEntity {
    private Order order;

    private TicketBatch batch;

    private int quantity;

    private BigDecimal unitPrice;
}
