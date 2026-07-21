package com.example.service;

import com.example.dto.WorkoutDto;
import com.example.model.User;
import com.example.model.Workout;
import com.example.repository.UserRepository;
import com.example.repository.WorkoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkoutService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutService.class);

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ModerationService moderationService;
    private final NotificationService notificationService;

    public WorkoutService(WorkoutRepository workoutRepository,
                          UserRepository userRepository,
                          ModerationService moderationService,
                          NotificationService notificationService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.moderationService = moderationService;
        this.notificationService = notificationService;
    }

    public List<Workout> getWorkoutsByTrainer(Long trainerId) {
        return workoutRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId);
    }

    public Workout createWorkout(Workout workout, User trainer) {
        ensureTrainerCanManageWorkouts(trainer);
        workout.setTrainer(trainer);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setActive(true);
        moderationService.evaluateWorkout(workout);
        return workoutRepository.save(workout);
    }

    public Workout updateWorkout(Long workoutId, Workout updates, User trainer) {
        ensureTrainerCanManageWorkouts(trainer);
        Workout workout = getById(workoutId);
        if (!workout.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Not allowed to edit this workout");
        }
        workout.setName(updates.getName());
        workout.setDescription(updates.getDescription());
        workout.setInstructions(updates.getInstructions());
        workout.setMuscleGroup(updates.getMuscleGroup());
        workout.setCategory(updates.getCategory());
        workout.setDifficulty(updates.getDifficulty());
        workout.setEquipment(updates.getEquipment());
        workout.setEstimatedCaloriesBurned(updates.getEstimatedCaloriesBurned());
        workout.setEstimatedDurationMinutes(updates.getEstimatedDurationMinutes());
        workout.setSets(updates.getSets());
        workout.setReps(updates.getReps());
        workout.setVideoUrl(updates.getVideoUrl());
        workout.setImageUrl(updates.getImageUrl());
        moderationService.evaluateWorkout(workout);
        return workoutRepository.save(workout);
    }

    public Workout getTrainerWorkout(Long workoutId, User trainer) {
        ensureTrainerCanManageWorkouts(trainer);
        Workout workout = getById(workoutId);
        if (!workout.getTrainer().getId().equals(trainer.getId())) {
            throw new RuntimeException("Not allowed to view this workout");
        }
        return workout;
    }

    @Transactional
    public void deleteTrainerWorkout(Long workoutId, User trainer) {
        getTrainerWorkout(workoutId, trainer);
        deleteWorkout(workoutId);
    }

    public void setWorkoutsActiveByTrainer(Long trainerId, boolean active) {
        List<Workout> trainerWorkouts = getWorkoutsByTrainer(trainerId);
        for (Workout workout : trainerWorkouts) {
            workout.setActive(active);
        }
        workoutRepository.saveAll(trainerWorkouts);
    }

    public List<Workout> getAllWorkouts() {
        return workoutRepository.findAll(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );
    }

    public Workout getById(Long workoutId) {
        return workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found with id: " + workoutId));
    }

    @Transactional
    public void deleteWorkout(Long workoutId) {
        workoutRepository.deleteById(workoutId);
    }

    public void ensureTrainerCanManageWorkouts(User trainer) {
        if (trainer == null || !trainer.isEmailVerified() || !trainer.isEnabled()
                || "PENDING".equalsIgnoreCase(trainer.getApprovalStatus())
                || "REJECTED".equalsIgnoreCase(trainer.getApprovalStatus())) {
            throw new RuntimeException("Trainer account must be approved before managing workouts");
        }
    }

    public Workout approveWorkout(Long workoutId) {
        Workout workout = getById(workoutId);
        workout.setRiskScore(0);
        workout.setSuspicious(false);
        workout.setModerationStatus("APPROVED");
        workout.setActive(true);
        return workoutRepository.save(workout);
    }

    public Workout blockWorkout(Long workoutId, String reason) {
        Workout workout = getById(workoutId);
        workout.setActive(false);
        workout.setSuspicious(true);
        workout.setModerationStatus("BLOCKED");
        workout.setModerationReason(reason == null || reason.isBlank() ? "Blocked by admin review" : reason.trim());
        return workoutRepository.save(workout);
    }

    public Workout rescanWorkout(Long workoutId) {
        Workout workout = getById(workoutId);
        moderationService.evaluateWorkout(workout);
        if ("BLOCKED".equalsIgnoreCase(workout.getModerationStatus())) {
            workout.setActive(false);
        }
        return workoutRepository.save(workout);
    }
}
