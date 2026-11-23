package com.projeto.ticket_wave.modules.events.domain.entity;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue extends BaseEntity {
    private String name;

    private String description;

    private Integer capacity;

    private String phone;

    private String email;

    private String slug;

    private String website;

    private Address address;
}
