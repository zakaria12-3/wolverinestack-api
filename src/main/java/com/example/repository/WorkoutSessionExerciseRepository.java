package com.example.repository;

import com.example.model.WorkoutSessionExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutSessionExerciseRepository extends JpaRepository<WorkoutSessionExercise, Long> {

    List<WorkoutSessionExercise> findBySessionIdOrderByOrderIndexAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
