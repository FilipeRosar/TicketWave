package com.projeto.ticket_wave.modules.organizations.domain;

import java.util.Set;

import static com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole.*;

public enum OrganizationPermission {
    MANAGE_MEMBERS(OWNER),
    READ_MEMBERS(OWNER, ADMIN),
    EDIT_COMMERCIAL_PROFILE(OWNER),
    READ_EVENTS(OWNER, ADMIN, OPERADOR),
    EDIT_DRAFTS(OWNER, ADMIN, OPERADOR),
    MANAGE_EVENTS(OWNER, ADMIN),
    READ_REPORTS(OWNER, ADMIN, FINANCEIRO);

    private final Set<OrganizationMemberRole> roles;

    OrganizationPermission(OrganizationMemberRole... roles) {
        this.roles = Set.of(roles);
    }

    public Set<OrganizationMemberRole> roles() {
        return roles;
    }
}
