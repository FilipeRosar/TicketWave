package com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity;

import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "Venue")
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
public class VenueJpaEntity extends JpaBaseEntity {
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
    private AddressJpaEntity address;
}
