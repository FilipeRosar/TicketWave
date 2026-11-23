package com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity;

import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity(name = "Address")
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class AddressJpaEntity extends JpaBaseEntity {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String number;

    private String complement;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String country = "Brazil";

    private Double latitude;

    private Double longitude;
}
