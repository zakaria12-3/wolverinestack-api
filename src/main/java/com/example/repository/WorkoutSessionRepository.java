package com.example.repository;

import com.example.model.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    List<WorkoutSession> findByMemberIdOrderByStartedAtDesc(Long memberId);

    List<WorkoutSession> findByMemberIdAndCompletedTrueOrderByStartedAtDesc(Long memberId);

    List<WorkoutSession> findByMemberIdAndStartedAtBetweenOrderByStartedAtAsc(
            Long memberId, LocalDateTime start, LocalDateTime end);

    long countByMemberIdAndCompletedTrue(Long memberId);

    void deleteByMemberId(Long memberId);
    boolean existsByWorkoutPlanIdAndMemberId(Long planId, Long memberId);

    @Query(value = """
            SELECT wse.exercise_name AS exerciseName,
                   es.logged_at AS loggedAt,
                   es.weight_kg AS weightKg,
                   es.reps AS reps,
                   es.set_index AS sets
            FROM exercise_sets es
            JOIN workout_session_exercises wse ON es.session_exercise_id = wse.id
            JOIN workout_sessions ws ON wse.session_id = ws.id
            WHERE ws.member_id = :memberId
              AND wse.exercise_name = :exerciseName
              AND es.weight_kg IS NOT NULL
            ORDER BY es.logged_at ASC
            """, nativeQuery = true)
    List<Object[]> findProgressForExercise(@Param("memberId") Long memberId,
                                           @Param("exerciseName") String exerciseName);

    @Query(value = """
            SELECT DISTINCT wse.exercise_name
            FROM workout_session_exercises wse
            JOIN workout_sessions ws ON wse.session_id = ws.id
            WHERE ws.member_id = :memberId
            ORDER BY wse.exercise_name ASC
            """, nativeQuery = true)
    List<String> findTrackedExercisesByMember(@Param("memberId") Long memberId);
}
