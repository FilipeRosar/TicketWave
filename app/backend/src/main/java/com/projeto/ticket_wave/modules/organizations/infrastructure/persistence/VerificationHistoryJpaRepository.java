package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationHistoryJpaRepository extends JpaRepository<VerificationHistoryJpaEntity, UUID> {
    List<VerificationHistoryJpaEntity> findAllByOrganizationIdOrderByCreatedDateAscIdAsc(UUID organizationId);
}
