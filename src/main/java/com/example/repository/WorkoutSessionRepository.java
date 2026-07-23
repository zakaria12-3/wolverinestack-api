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

    /** Get the all-time best set per exercise for a member (one PR per exercise) */
    @Query(value = """
            SELECT exerciseName, muscleGroup, equipment, weightKg, reps,
                   estimatedOneRm, achievedDate, sessionId, sessionName,
                   totalSessionsLogged, totalSetsLogged
            FROM (
                SELECT wse.exercise_name AS exerciseName,
                       wse.muscle_group AS muscleGroup,
                       wse.equipment AS equipment,
                       es.weight_kg AS weightKg,
                       es.reps AS reps,
                       ROUND(es.weight_kg * (1.0 + es.reps / 30.0)) AS estimatedOneRm,
                       es.logged_at AS achievedDate,
                       ws.id AS sessionId,
                       ws.session_name AS sessionName,
                       (SELECT COUNT(*) FROM workout_sessions ws2
                        JOIN workout_session_exercises wse2 ON ws2.id = wse2.session_id
                        WHERE ws2.member_id = :memberId AND wse2.exercise_name = wse.exercise_name) AS totalSessionsLogged,
                       (SELECT COUNT(*) FROM exercise_sets es3
                        JOIN workout_session_exercises wse3 ON es3.session_exercise_id = wse3.id
                        JOIN workout_sessions ws3 ON wse3.session_id = ws3.id
                        WHERE ws3.member_id = :memberId AND wse3.exercise_name = wse.exercise_name
                          AND es3.weight_kg IS NOT NULL AND es3.reps IS NOT NULL) AS totalSetsLogged,
                       ROW_NUMBER() OVER (PARTITION BY wse.exercise_name
                           ORDER BY ROUND(es.weight_kg * (1.0 + es.reps / 30.0)) DESC) AS rn
                FROM exercise_sets es
                JOIN workout_session_exercises wse ON es.session_exercise_id = wse.id
                JOIN workout_sessions ws ON wse.session_id = ws.id
                WHERE ws.member_id = :memberId
                  AND es.weight_kg IS NOT NULL
                  AND es.reps IS NOT NULL
                  AND es.reps > 0
            ) ranked
            WHERE rn = 1
            ORDER BY estimatedOneRm DESC
            """, nativeQuery = true)
    List<Object[]> findAllTimeBestPerExercise(@Param("memberId") Long memberId);

    /** Get all progression data points for a member's exercise (for charts) */
    @Query(value = """
            SELECT wse.exercise_name AS exerciseName,
                   CAST(es.logged_at AS date) AS loggedDate,
                   MAX(ROUND(es.weight_kg * (1.0 + es.reps / 30.0))) AS bestOneRm
            FROM exercise_sets es
            JOIN workout_session_exercises wse ON es.session_exercise_id = wse.id
            JOIN workout_sessions ws ON wse.session_id = ws.id
            WHERE ws.member_id = :memberId
              AND wse.exercise_name = :exerciseName
              AND es.weight_kg IS NOT NULL
              AND es.reps IS NOT NULL
              AND es.reps > 0
            GROUP BY wse.exercise_name, CAST(es.logged_at AS date)
            ORDER BY loggedDate ASC
            """, nativeQuery = true)
    List<Object[]> findProgressionForExerciseChart(@Param("memberId") Long memberId,
                                                    @Param("exerciseName") String exerciseName);
}
