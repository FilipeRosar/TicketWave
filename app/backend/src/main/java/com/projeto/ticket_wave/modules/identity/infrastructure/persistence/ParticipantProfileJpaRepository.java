package com.projeto.ticket_wave.modules.identity.infrastructure.persistence;

import com.projeto.ticket_wave.modules.identity.infrastructure.persistence.entity.ParticipantProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface ParticipantProfileJpaRepository extends JpaRepository<ParticipantProfileJpaEntity, UUID> {
    @Query(value = """
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
            SELECT user_id AS "userId", name, occupation, city,
                   shared_interests AS "sharedInterests", matched_goals AS "matchedGoals"
            FROM matches
            WHERE cardinality(shared_interests) + cardinality(matched_goals) > 0
            ORDER BY cardinality(shared_interests) + cardinality(matched_goals) DESC, name, user_id
            LIMIT 12
            """, nativeQuery = true)
    List<SuggestionRow> findSuggestions(UUID userId);

    interface SuggestionRow {
        UUID getUserId();
        String getName();
        String getOccupation();
        String getCity();
        String[] getSharedInterests();
        String[] getMatchedGoals();
    }
}
