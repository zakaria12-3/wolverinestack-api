package com.example.controller;

import com.example.dto.PlatformRatingSummaryDto;
import com.example.model.Workout;
import com.example.repository.WorkoutRepository;
import com.example.service.PlatformRatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final WorkoutRepository workoutRepository;
    private final PlatformRatingService platformRatingService;

    public PublicController(WorkoutRepository workoutRepository, PlatformRatingService platformRatingService) {
        this.workoutRepository = workoutRepository;
        this.platformRatingService = platformRatingService;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Workout> workouts = workoutRepository.findVisibleWorkouts();
        PlatformRatingSummaryDto ratings = platformRatingService.getSummary();

        return Map.of(
                "totalWorkouts", workouts.size(),
                "recentWorkouts", workouts.stream().limit(5).map(w -> Map.of(
                        "id", w.getId(),
                        "name", w.getName(),
                        "category", w.getCategory() != null ? w.getCategory() : "",
                        "difficulty", w.getDifficulty() != null ? w.getDifficulty() : "",
                        "muscleGroup", w.getMuscleGroup() != null ? w.getMuscleGroup() : ""
                )).toList(),
                "averageRating", ratings.getAverageRating(),
                "ratingCount", ratings.getRatingCount()
        );
    }

    @GetMapping("/hero-slides")
    public List<Map<String, String>> heroSlides() {
        return List.of(
                Map.of(
                        "role", "MEMBER",
                        "title", "Member Journey",
                        "subtitle", "Track workouts, log meals, and reach your fitness goals",
                        "imageUrl", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=900&q=80"
                ),
                Map.of(
                        "role", "TRAINER",
                        "title", "Trainer Space",
                        "subtitle", "Create workouts, design plans, and grow your community",
                        "imageUrl", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=900&q=80"
                ),
                Map.of(
                        "role", "COMMUNITY",
                        "title", "Fitness Community",
                        "subtitle", "Share progress, get inspired, and stay accountable together",
                        "imageUrl", "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=900&q=80"
                )
        );
    }

    @GetMapping("/workouts")
    public List<Workout> workouts() {
        return workoutRepository.findVisibleWorkouts();
    }

    @GetMapping("/workouts/{id}")
    public Workout workout(@PathVariable Long id) {
        return workoutRepository.findById(id)
                .filter(w -> Boolean.TRUE.equals(w.getActive()))
                .filter(w -> w.getModerationStatus() == null || "APPROVED".equalsIgnoreCase(w.getModerationStatus()))
                .orElseThrow(() -> new RuntimeException("Workout not found"));
    }
}
