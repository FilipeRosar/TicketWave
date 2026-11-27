package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.application.dto.ParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.port.ParticipantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ParticipantProfilePersistenceAdapter implements ParticipantProfileRepository {
    private final JdbcClient jdbc;

    @Override
    public Optional<ParticipantProfile> findByUserId(UUID userId) {
        return jdbc.sql("""
                SELECT name, occupation, city, bio, photo_type, photo
                FROM participant_profiles WHERE user_id = :userId
                """).param("userId", userId).query(ParticipantProfile.class).optional();
    }

    @Override
    public ParticipantProfile save(UUID userId, ParticipantProfile profile, boolean replacePhoto) {
        return jdbc.sql("""
                INSERT INTO participant_profiles AS current_profile (user_id, name, occupation, city, bio, photo_type, photo)
                VALUES (:userId, :name, :occupation, :city, :bio, :photoType, :photo)
                ON CONFLICT (user_id) DO UPDATE SET
                    name = EXCLUDED.name,
                    occupation = EXCLUDED.occupation,
                    city = EXCLUDED.city,
                    bio = EXCLUDED.bio,
                    photo_type = CASE WHEN :replacePhoto THEN EXCLUDED.photo_type ELSE current_profile.photo_type END,
                    photo = CASE WHEN :replacePhoto THEN EXCLUDED.photo ELSE current_profile.photo END
                RETURNING name, occupation, city, bio, photo_type, photo
                """)
                .param("userId", userId)
                .param("name", profile.name())
                .param("occupation", profile.occupation())
                .param("city", profile.city())
                .param("bio", profile.bio())
                .param("photoType", profile.photoType(), Types.VARCHAR)
                .param("photo", profile.photo(), Types.BINARY)
                .param("replacePhoto", replacePhoto)
                .query(ParticipantProfile.class).single();
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jdbc.sql("DELETE FROM participant_profiles WHERE user_id = :userId")
                .param("userId", userId).update();
    }
}
