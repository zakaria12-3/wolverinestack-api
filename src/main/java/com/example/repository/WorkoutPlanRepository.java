package com.example.repository;

import com.example.model.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    java.util.Optional<WorkoutPlan> findById(Long id);
}
