package com.projeto.ticket_wave.shared.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class BaseEntity {
    private UUID id;

    private Instant createdDate;

    private Instant updatedAt;

    private Long version;

}
