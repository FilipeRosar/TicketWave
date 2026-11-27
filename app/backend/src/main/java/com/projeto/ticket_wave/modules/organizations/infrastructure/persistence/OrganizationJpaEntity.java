package com.projeto.ticket_wave.modules.organizations.infrastructure.persistence;

import com.projeto.ticket_wave.modules.organizations.domain.OrganizationFinancialStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationStatus;
import com.projeto.ticket_wave.modules.organizations.domain.OrganizationType;
import com.projeto.ticket_wave.shared.persistence.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organizations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationJpaEntity extends JpaBaseEntity {
    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private OrganizationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OrganizationStatus status;

    @Column(name = "document_ciphertext")
    private byte[] documentCiphertext;

    @Column(name = "commercial_name", length = 150)
    private String commercialName;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "commercial_description", length = 1000)
    private String commercialDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_status", nullable = false, length = 12)
    private OrganizationFinancialStatus financialStatus = OrganizationFinancialStatus.PENDING;

    public void updateCommercialProfile(String name, String email, String description) {
        commercialName = name;
        contactEmail = email;
        commercialDescription = description;
    }

    public OrganizationJpaEntity(String name, OrganizationType type, byte[] documentCiphertext) {
        this.name = name;
        this.type = type;
        this.status = OrganizationStatus.ACTIVE;
        this.documentCiphertext = documentCiphertext;
    }
}
