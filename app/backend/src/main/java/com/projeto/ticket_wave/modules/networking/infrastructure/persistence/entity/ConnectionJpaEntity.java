package com.projeto.ticket_wave.modules.networking.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.EventJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import com.projeto.ticket_wave.modules.networking.domain.enums.ConnectionStatus;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "Connection")
@Table(name = "connections")
@Getter
@Setter
@NoArgsConstructor
public class ConnectionJpaEntity extends JpaBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserJpaEntity requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserJpaEntity recipient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_event_id", nullable = false)
    private EventJpaEntity sourceEvent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectionStatus status;
}
