package com.projeto.ticket_wave.modules.identity.domain.entity;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {
    private String token;

    private Instant expiresAt;

    private boolean revoked;

    private User user;
}
