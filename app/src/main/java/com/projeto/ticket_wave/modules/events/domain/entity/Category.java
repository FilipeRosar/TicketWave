package com.projeto.ticket_wave.modules.events.domain.entity;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {
    private String name;

    private String slug;

    private String description;

    @Builder.Default
    private Boolean active = true;
}
