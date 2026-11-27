package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.application.dto.ConnectionSuggestion;
import com.projeto.ticket_wave.modules.identity.application.dto.ParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.port.ParticipantProfileRepository;
import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.ParticipantProfileJpaEntity;
import com.projeto.ticket_wave.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ParticipantProfilePersistenceAdapter implements ParticipantProfileRepository {
    private final ParticipantProfileJpaRepository profiles;
    private final UserJpaRepository users;

    @Override
    public Optional<ParticipantProfile> findByUserId(UUID userId) {
        return profiles.findById(userId).map(this::toProfile);
    }

    @Override
    @Transactional
    public ParticipantProfile save(UUID userId, ParticipantProfile profile, boolean replacePhoto) {
        lockUser(userId);
        var entity = profiles.findById(userId).orElseGet(() -> new ParticipantProfileJpaEntity(userId));
        entity.setName(profile.name());
        entity.setOccupation(profile.occupation());
        entity.setCity(profile.city());
        entity.setBio(profile.bio());
        entity.setInterests(profile.interests().toArray(String[]::new));
        entity.setGoals(profile.goals().toArray(String[]::new));
        if (replacePhoto) {
            entity.setPhotoType(profile.photoType());
            entity.setPhoto(profile.photo());
        }
        return toProfile(profiles.save(entity));
    }

    @Override
    public List<ConnectionSuggestion> findSuggestions(UUID userId) {
        return profiles.findSuggestions(userId).stream()
                .map(row -> new ConnectionSuggestion(
                        row.getUserId(), row.getName(), row.getOccupation(), row.getCity(),
                        List.of(row.getSharedInterests()), List.of(row.getMatchedGoals())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        lockUser(userId);
        profiles.deleteById(userId);
    }

    private void lockUser(UUID userId) {
        users.findForSessionUpdate(userId)
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .orElseThrow(() -> new UnauthorizedException("Usuário indisponível"));
    }

    private ParticipantProfile toProfile(ParticipantProfileJpaEntity entity) {
        return new ParticipantProfile(entity.getName(), entity.getOccupation(), entity.getCity(), entity.getBio(),
                entity.getPhotoType(), entity.getPhoto(), List.of(entity.getInterests()), List.of(entity.getGoals()));
    }
}
