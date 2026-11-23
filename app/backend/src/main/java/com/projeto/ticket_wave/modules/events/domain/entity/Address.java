package com.projeto.ticket_wave.modules.events.domain.entity;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseEntity {

    private String street;

    private String number;

    private String complement;

    private String district;

    private String city;

    private String state;

    private String zipCode;

    private String country = "Brazil";

    private Double latitude;

    private Double longitude;
}
