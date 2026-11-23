package com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity;

import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false)
    private Boolean enabled = true;
}
