package com.projeto.ticket_wave.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue extends BaseEntity {
    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer capacity;

    private String phone;

    private String email;

    @Column(nullable = false, unique = true)
    private String slug;

    private String website;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;
}
