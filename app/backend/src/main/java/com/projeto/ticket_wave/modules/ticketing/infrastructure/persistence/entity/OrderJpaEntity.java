package com.projeto.ticket_wave.modules.ticketing.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.modules.ticketing.domain.enums.OrderStatus;
import com.projeto.ticket_wave.modules.ticketing.domain.enums.PaymentMethod;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "Order")
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity extends JpaBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserJpaEntity buyer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;
}
