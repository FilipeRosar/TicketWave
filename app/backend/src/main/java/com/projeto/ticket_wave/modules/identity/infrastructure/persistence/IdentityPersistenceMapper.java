package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.domain.entity.RefreshToken;
import com.projeto.ticket_wave.modules.identity.domain.entity.User;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IdentityPersistenceMapper {
    User toDomain(UserJpaEntity source);

    RefreshToken toDomain(RefreshTokenJpaEntity source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "user", ignore = true)
    void copyToJpa(RefreshToken source, @MappingTarget RefreshTokenJpaEntity target);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void copyToJpa(User source, @MappingTarget UserJpaEntity target);
}
