package com.projeto.ticket_wave.shared.persistence;

import com.projeto.ticket_wave.shared.domain.BaseEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class JpaEntityAccess {
    private final EntityManager entityManager;

    public <T extends JpaBaseEntity> T forWrite(BaseEntity source, Class<T> type, Supplier<T> factory) {
        return source.getId() == null ? factory.get() : existing(source, type);
    }

    public <T extends JpaBaseEntity> T existing(BaseEntity source, Class<T> type) {
        T entity = entityManager.find(type, source.getId());
        if (entity == null || !Objects.equals(source.getVersion(), entity.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(type, source.getId());
        }
        return entity;
    }

    public <T extends JpaBaseEntity> T reference(BaseEntity source, Class<T> type) {
        return source == null ? null : entityManager.getReference(type, source.getId());
    }
}
