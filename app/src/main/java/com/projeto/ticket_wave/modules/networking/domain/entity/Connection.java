package com.projeto.ticket_wave.modules.networking.domain.entity;

import com.projeto.ticket_wave.modules.events.domain.entity.Event;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.networking.domain.enums.ConnectionStatus;
import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connection extends BaseEntity {
    private User requester;

    private User recipient;

    private Event sourceEvent;

    private ConnectionStatus status;
}
