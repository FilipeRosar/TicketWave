package com.projeto.ticket_wave.modules.ticketing.domain.entity;

import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.ticketing.domain.enums.OrderStatus;
import com.projeto.ticket_wave.modules.ticketing.domain.enums.PaymentMethod;
import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {
    private User buyer;

    private BigDecimal total;

    private OrderStatus status;

    private PaymentMethod paymentMethod;
}
