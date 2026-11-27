package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationMemberRole;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "organization_members", uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationMemberJpaEntity extends JpaBaseEntity {
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrganizationMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrganizationStatus status;

    public OrganizationMemberJpaEntity(UUID organizationId, UUID userId, OrganizationMemberRole role) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.status = OrganizationStatus.ACTIVE;
    }

    public void activate(OrganizationMemberRole role) {
        this.role = role;
        this.status = OrganizationStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = OrganizationStatus.INACTIVE;
    }
}
