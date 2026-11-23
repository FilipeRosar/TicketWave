package com.projeto.ticket_wave.modules.events.infrastructure.persistence;

import com.projeto.ticket_wave.modules.events.infrastructure.persistence.entity.AddressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, UUID> {

}
