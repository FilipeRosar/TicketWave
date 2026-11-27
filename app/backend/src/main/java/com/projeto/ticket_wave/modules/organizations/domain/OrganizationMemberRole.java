package com.projeto.ticket_wave.modules.organizations.domain;

import java.util.Set;

public enum OrganizationMemberRole {
    OWNER,
    ADMIN,
    FINANCEIRO,
    OPERADOR;

    public static final Set<OrganizationMemberRole> EVENT_MANAGERS = Set.of(OWNER, ADMIN);
}
