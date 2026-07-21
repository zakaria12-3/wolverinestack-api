package com.example.repository;

import com.example.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);

    List<Workout> findByMuscleGroupIgnoreCase(String muscleGroup);
    List<Workout> findByCategoryIgnoreCase(String category);
    List<Workout> findByDifficultyIgnoreCase(String difficulty);

    long countByNameAndDescription(String name, String description);

    @Query("""
            select w from Workout w
            where (w.active is null or w.active = true)
              and (w.moderationStatus is null or w.moderationStatus = 'APPROVED')
            order by w.createdAt desc
            """)
    List<Workout> findVisibleWorkouts();

    @Query("""
            select w from Workout w
            where (lower(w.name) like lower(concat('%', :query, '%'))
                or lower(w.muscleGroup) like lower(concat('%', :query, '%')))
              and (w.active is null or w.active = true)
              and (w.moderationStatus is null or w.moderationStatus = 'APPROVED')
            order by w.createdAt desc
            """)
    List<Workout> searchVisibleWorkouts(@Param("query") String query);
}
