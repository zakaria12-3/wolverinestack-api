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
            SELECT le.exercise_name AS exerciseName,
                   le.logged_at AS loggedAt,
                   le.weight_kg AS weightKg,
                   le.actual_reps AS reps,
                   le.actual_sets AS sets
            FROM workout_log_entries le
            JOIN workout_sessions ws ON le.session_id = ws.id
            WHERE ws.member_id = :memberId
              AND le.exercise_name = :exerciseName
              AND le.weight_kg IS NOT NULL
            ORDER BY le.logged_at ASC
            """, nativeQuery = true)
    List<Object[]> findProgressForExercise(@Param("memberId") Long memberId,
                                           @Param("exerciseName") String exerciseName);

    @Query(value = """
            SELECT DISTINCT le.exercise_name
            FROM workout_log_entries le
            JOIN workout_sessions ws ON le.session_id = ws.id
            WHERE ws.member_id = :memberId
            ORDER BY le.exercise_name ASC
            """, nativeQuery = true)
    List<String> findTrackedExercisesByMember(@Param("memberId") Long memberId);
}
