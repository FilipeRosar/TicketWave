package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.application.dto.ConnectionSuggestion;
import com.projeto.ticket_wave.modules.identity.application.dto.ParticipantProfile;
import com.projeto.ticket_wave.modules.identity.application.port.ParticipantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.SqlArrayValue;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ParticipantProfilePersistenceAdapter implements ParticipantProfileRepository {
    private static final RowMapper<ParticipantProfile> PROFILE_ROW = (row, index) -> new ParticipantProfile(
            row.getString("name"), row.getString("occupation"), row.getString("city"), row.getString("bio"),
            row.getString("photo_type"), row.getBytes("photo"), readArray(row, "interests"), readArray(row, "goals"));

    private final JdbcClient jdbc;

    @Override
    public Optional<ParticipantProfile> findByUserId(UUID userId) {
        return jdbc.sql("""
                SELECT name, occupation, city, bio, photo_type, photo, interests, goals
                FROM participant_profiles WHERE user_id = :userId
                """).param("userId", userId).query(PROFILE_ROW).optional();
    }

    @Override
    public ParticipantProfile save(UUID userId, ParticipantProfile profile, boolean replacePhoto) {
        return jdbc.sql("""
                INSERT INTO participant_profiles AS current_profile (user_id, name, occupation, city, bio, photo_type, photo, interests, goals)
                VALUES (:userId, :name, :occupation, :city, :bio, :photoType, :photo, :interests, :goals)
                ON CONFLICT (user_id) DO UPDATE SET
                    name = EXCLUDED.name,
                    occupation = EXCLUDED.occupation,
                    city = EXCLUDED.city,
                    bio = EXCLUDED.bio,
                    interests = EXCLUDED.interests,
                    goals = EXCLUDED.goals,
                    photo_type = CASE WHEN :replacePhoto THEN EXCLUDED.photo_type ELSE current_profile.photo_type END,
                    photo = CASE WHEN :replacePhoto THEN EXCLUDED.photo ELSE current_profile.photo END
                RETURNING name, occupation, city, bio, photo_type, photo, interests, goals
                """)
                .param("userId", userId)
                .param("name", profile.name())
                .param("occupation", profile.occupation())
                .param("city", profile.city())
                .param("bio", profile.bio())
                .param("photoType", profile.photoType(), Types.VARCHAR)
                .param("photo", profile.photo(), Types.BINARY)
                .param("interests", new SqlArrayValue("text", profile.interests().toArray()))
                .param("goals", new SqlArrayValue("text", profile.goals().toArray()))
                .param("replacePhoto", replacePhoto)
                .query(PROFILE_ROW).single();
    }

    @Override
    public List<ConnectionSuggestion> findSuggestions(UUID userId) {
        return jdbc.sql("""
                WITH matches AS (
                    SELECT candidate.user_id, candidate.name, candidate.occupation, candidate.city,
                           ARRAY(SELECT interest FROM unnest(own.interests) AS item(interest)
                                 WHERE interest = ANY(candidate.interests)) AS shared_interests,
                           ARRAY(SELECT goal FROM unnest(candidate.goals) AS item(goal)
                                 WHERE CASE goal
                                     WHEN 'HIRE' THEN 'GET_HIRED'
                                     WHEN 'GET_HIRED' THEN 'HIRE'
                                     ELSE goal END = ANY(own.goals)) AS matched_goals
                    FROM participant_profiles own
                    JOIN participant_profiles candidate ON candidate.user_id <> own.user_id
                    JOIN users candidate_user ON candidate_user.id = candidate.user_id
                    WHERE own.user_id = :userId
                      AND candidate_user.enabled AND candidate_user.role = 'ROLE_CUSTOMER'
                      AND NOT EXISTS (
                          SELECT 1 FROM connections existing
                          WHERE LEAST(existing.requester_id, existing.recipient_id) = LEAST(own.user_id, candidate.user_id)
                            AND GREATEST(existing.requester_id, existing.recipient_id) = GREATEST(own.user_id, candidate.user_id)
                      )
                )
                SELECT * FROM matches
                WHERE cardinality(shared_interests) + cardinality(matched_goals) > 0
                ORDER BY cardinality(shared_interests) + cardinality(matched_goals) DESC, name, user_id
                LIMIT 12
                """).param("userId", userId)
                .query((row, index) -> new ConnectionSuggestion(
                        row.getObject("user_id", UUID.class), row.getString("name"),
                        row.getString("occupation"), row.getString("city"),
                        readArray(row, "shared_interests"), readArray(row, "matched_goals")))
                .list();
    }

    private static List<String> readArray(ResultSet row, String column) throws SQLException {
        var array = row.getArray(column);
        try {
            return List.of((String[]) array.getArray());
        } finally {
            array.free();
        }
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jdbc.sql("DELETE FROM participant_profiles WHERE user_id = :userId")
                .param("userId", userId).update();
    }
}
