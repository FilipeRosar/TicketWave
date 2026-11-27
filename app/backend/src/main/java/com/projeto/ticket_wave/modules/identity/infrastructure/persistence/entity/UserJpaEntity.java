package com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.identity.domain.entity.RegistrationAddress;
import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "User")
@Table(name = "users")
public class UserJpaEntity extends JpaBaseEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "google_subject", unique = true)
    private String googleSubject;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(unique = true)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 11, unique = true)
    private String cpf;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "registration_address")
    private RegistrationAddress registrationAddress;

    @Column(nullable = false)
    private Boolean enabled = true;
}
