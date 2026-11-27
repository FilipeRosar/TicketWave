package com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "participant_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantProfileJpaEntity {
    @Id
    @Column(name = "user_id")
    @Setter(AccessLevel.NONE)
    private UUID userId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 120)
    private String occupation;

    @Column(length = 120)
    private String city;

    @Column(length = 1000)
    private String bio;

    @Column(name = "photo_type", length = 20)
    private String photoType;

    private byte[] photo;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private String[] interests = {};

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private String[] goals = {};

    public ParticipantProfileJpaEntity(UUID userId) {
        this.userId = userId;
    }
}
