package com.example.controller;

import com.example.dto.CreateWorkoutPlanDto;
import com.example.dto.WorkoutDto;
import com.example.model.User;
import com.example.model.Workout;
import com.example.model.WorkoutPlan;
import com.example.repository.UserRepository;
import com.example.service.WorkoutPlanService;
import com.example.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trainer")
public class TrainerController {

    private final WorkoutService workoutService;
    private final WorkoutPlanService workoutPlanService;
    private final UserRepository userRepository;

    public TrainerController(WorkoutService workoutService,
                             WorkoutPlanService workoutPlanService,
                             UserRepository userRepository) {
        this.workoutService = workoutService;
        this.workoutPlanService = workoutPlanService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Trainer dashboard";
    }

    // Workout CRUD
    @GetMapping("/workouts")
    public List<Workout> getTrainerWorkouts(Authentication authentication) {
        User trainer = currentTrainer(authentication);
        workoutService.ensureTrainerCanManageWorkouts(trainer);
        return workoutService.getWorkoutsByTrainer(trainer.getId());
    }

    @PostMapping("/workouts")
    public Workout createWorkout(@RequestBody WorkoutDto dto, Authentication authentication) {
        User trainer = currentTrainer(authentication);
        Workout workout = new Workout();
        workout.setName(dto.getName());
        workout.setDescription(dto.getDescription());
        workout.setInstructions(dto.getInstructions());
        workout.setMuscleGroup(dto.getMuscleGroup());
        workout.setCategory(dto.getCategory());
        workout.setDifficulty(dto.getDifficulty());
        workout.setEquipment(dto.getEquipment());
        workout.setEstimatedCaloriesBurned(dto.getEstimatedCaloriesBurned());
        workout.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        workout.setSets(dto.getSets());
        workout.setReps(dto.getReps());
        workout.setVideoUrl(dto.getVideoUrl());
        workout.setImageUrl(dto.getImageUrl());
        return workoutService.createWorkout(workout, trainer);
    }

    @GetMapping("/workouts/{id}")
    public Workout getWorkout(@PathVariable Long id, Authentication authentication) {
        User trainer = currentTrainer(authentication);
        return workoutService.getTrainerWorkout(id, trainer);
    }

    @PutMapping("/workouts/{id}")
    public Workout updateWorkout(@PathVariable Long id, @RequestBody WorkoutDto dto,
                                 Authentication authentication) {
        User trainer = currentTrainer(authentication);
        Workout workout = new Workout();
        workout.setName(dto.getName());
        workout.setDescription(dto.getDescription());
        workout.setInstructions(dto.getInstructions());
        workout.setMuscleGroup(dto.getMuscleGroup());
        workout.setCategory(dto.getCategory());
        workout.setDifficulty(dto.getDifficulty());
        workout.setEquipment(dto.getEquipment());
        workout.setEstimatedCaloriesBurned(dto.getEstimatedCaloriesBurned());
        workout.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        workout.setSets(dto.getSets());
        workout.setReps(dto.getReps());
        workout.setVideoUrl(dto.getVideoUrl());
        workout.setImageUrl(dto.getImageUrl());
        return workoutService.updateWorkout(id, workout, trainer);
    }

    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id, Authentication authentication) {
        workoutService.deleteTrainerWorkout(id, currentTrainer(authentication));
        return ResponseEntity.ok().build();
    }

    // Workout Plans
    @PostMapping("/plans")
    public CreateWorkoutPlanDto createPlan(@RequestBody CreateWorkoutPlanDto dto,
                                           Authentication authentication) {
        User trainer = currentTrainer(authentication);
        workoutService.ensureTrainerCanManageWorkouts(trainer);
        dto.setTrainerId(trainer.getId());
        workoutPlanService.createPlan(dto);
        return dto;
    }

    @GetMapping("/plans/{planId}")
    public com.example.dto.WorkoutPlanDto getPlan(@PathVariable Long planId) {
        return workoutPlanService.getPlanDtoById(planId);
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<?> generatePlan(@RequestBody Map<String, Object> body) {
        try {
            String goal = (String) body.getOrDefault("goal", "general_fitness");
            String difficulty = (String) body.getOrDefault("difficulty", "beginner");
            int weeks = body.get("durationWeeks") instanceof Number n ? n.intValue() : 4;
            int sessions = body.get("sessionsPerWeek") instanceof Number n ? n.intValue() : 3;
            return ResponseEntity.ok(
                    workoutPlanService.generatePlanDraft(goal, difficulty, weeks, sessions)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "hint", "If the error mentions 'no JSON found', the AI model returned plain text. Try again."
            ));
        }
    }

    private User currentTrainer(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
