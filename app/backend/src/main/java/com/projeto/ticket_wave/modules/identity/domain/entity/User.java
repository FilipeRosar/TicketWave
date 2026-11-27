package com.projeto.ticket_wave.modules.identity.domain.entity;

import com.projeto.ticket_wave.modules.identity.domain.enums.Role;
import com.projeto.ticket_wave.shared.domain.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {
    private String name;

    private String email;

    private String password;

    private Role role;

    private String phone;

    private LocalDate birthDate;

    private String cpf;

    private Boolean enabled = true;
}
