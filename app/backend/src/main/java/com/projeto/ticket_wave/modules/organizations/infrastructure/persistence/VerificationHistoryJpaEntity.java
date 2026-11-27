package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import com.projeto.ticket_wave.modules.organizations.domain.VerificationReason;
import com.projeto.ticket_wave.modules.organizations.domain.VerificationStatus;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "organization_verification_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationHistoryJpaEntity extends JpaBaseEntity {
    @Column(nullable = false, updatable = false)
    private UUID organizationId;
    @Column(nullable = false, updatable = false)
    private UUID actorId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private VerificationStatus previousStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private VerificationStatus nextStatus;
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private VerificationReason reason;

    public VerificationHistoryJpaEntity(UUID organizationId, UUID actorId, VerificationStatus previousStatus,
                                        VerificationStatus nextStatus, VerificationReason reason) {
        this.organizationId = organizationId;
        this.actorId = actorId;
        this.previousStatus = previousStatus;
        this.nextStatus = nextStatus;
        this.reason = reason;
    }
}
