package com.example.repository;

import com.example.model.ExerciseSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {

    List<ExerciseSet> findBySessionExerciseIdOrderBySetIndexAsc(Long sessionExerciseId);

    void deleteBySessionExerciseId(Long sessionExerciseId);

    long countBySessionExerciseId(Long sessionExerciseId);

    /** Find the best set (by weight × reps volume) for a given exercise across a member's history */
    // The progress query is done via native query in WorkoutSessionRepository
}
