package com.example.service;

import com.example.dto.CreateWorkoutPlanDto;
import com.example.dto.ExerciseDto;
import com.example.dto.WorkoutPlanDto;
import com.example.dto.WorkoutSessionResultDto;
import com.example.model.*;
import com.example.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WorkoutPlanService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutPlanService.class);

    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final WorkoutSessionRepository sessionRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FitnessAIService fitnessAIService;

    public WorkoutPlanService(WorkoutPlanRepository workoutPlanRepository,
                              WorkoutExerciseRepository exerciseRepository,
                              WorkoutRepository workoutRepository,
                              UserRepository userRepository,
                              WorkoutSessionRepository sessionRepository,
                              MealEntryRepository mealEntryRepository,
                              FitnessAIService fitnessAIService) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.fitnessAIService = fitnessAIService;
    }

    public List<WorkoutPlan> getAllPlans() {
        return workoutPlanRepository.findAll();
    }

    public WorkoutPlan getPlanEntityById(Long planId) {
        return workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found"));
    }

    public WorkoutPlanDto getPlanDtoById(Long planId) {
        WorkoutPlan plan = getPlanEntityById(planId);
        List<ExerciseDto> exerciseDtos = plan.getExercises().stream()
                .map(this::mapToExerciseDto)
                .toList();
        return mapToPlanDto(plan, exerciseDtos);
    }

    public WorkoutPlan createPlan(CreateWorkoutPlanDto dto) {
        User trainer = userRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        WorkoutPlan plan = new WorkoutPlan();
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setGoal(dto.getGoal() != null ? FitnessGoal.valueOf(dto.getGoal().toUpperCase()) : null);
        plan.setDifficulty(dto.getDifficulty());
        plan.setDurationWeeks(dto.getDurationWeeks());
        plan.setSessionsPerWeek(dto.getSessionsPerWeek());
        plan.setEstimatedDailyCalories(dto.getEstimatedDailyCalories());
        plan.setTrainer(trainer);

        List<WorkoutExercise> exercises = new ArrayList<>();
        if (dto.getExercises() != null) {
            for (ExerciseDto exDto : dto.getExercises()) {
                WorkoutExercise ex = new WorkoutExercise();
                ex.setExerciseName(exDto.getExerciseName());
                ex.setDescription(exDto.getDescription());
                ex.setInstructions(exDto.getInstructions());
                ex.setSets(exDto.getSets());
                ex.setReps(exDto.getReps());
                ex.setDurationSeconds(exDto.getDurationSeconds());
                ex.setRestSeconds(exDto.getRestSeconds());
                ex.setMuscleGroup(exDto.getMuscleGroup());
                ex.setEquipment(exDto.getEquipment());
                ex.setOrderIndex(exDto.getOrderIndex());
                ex.setWorkoutPlan(plan);
                exercises.add(ex);
            }
        }

        plan.setExercises(exercises);
        return workoutPlanRepository.save(plan);
    }

    public WorkoutPlanDto generatePlanDraft(String goal, String difficulty,
                                            Integer durationWeeks, Integer sessionsPerWeek) throws Exception {
        Map<String, Object> aiResult = fitnessAIService.generateWorkoutPlan(
                goal, difficulty, durationWeeks, sessionsPerWeek);

        String name = (String) aiResult.getOrDefault("name", "AI Generated Plan");
        String desc = (String) aiResult.getOrDefault("description", "");
        Integer aiDurationWeeks = aiResult.get("durationWeeks") instanceof Number n ? n.intValue() : durationWeeks;
        Integer aiSessionsPerWeek = aiResult.get("sessionsPerWeek") instanceof Number n ? n.intValue() : sessionsPerWeek;
        Integer dailyCalories = aiResult.get("estimatedDailyCalories") instanceof Number n ? n.intValue() : null;

        List<ExerciseDto> exerciseDtos = new ArrayList<>();
        if (aiResult.get("exercises") instanceof List<?> exercises) {
            for (Object ex : exercises) {
                if (ex instanceof Map<?, ?> exMap) {
                    ExerciseDto exDto = new ExerciseDto();
                    exDto.setExerciseName((String) exMap.get("exerciseName"));
                    exDto.setDescription((String) exMap.get("description"));
                    exDto.setSets(exMap.get("sets") instanceof Number n ? n.intValue() : null);
                    exDto.setReps(exMap.get("reps") instanceof Number n ? n.intValue() : null);
                    exDto.setDurationSeconds(exMap.get("durationSeconds") instanceof Number n ? n.intValue() : 0);
                    exDto.setRestSeconds(exMap.get("restSeconds") instanceof Number n ? n.intValue() : 60);
                    exDto.setMuscleGroup((String) exMap.get("muscleGroup"));
                    exDto.setEquipment((String) exMap.get("equipment"));
                    exDto.setOrderIndex(exMap.get("orderIndex") instanceof Number n ? n.intValue() : 0);
                    exerciseDtos.add(exDto);
                }
            }
        }

        WorkoutPlanDto result = new WorkoutPlanDto();
        result.setName(name);
        result.setDescription(desc);
        result.setGoal(goal);
        result.setDifficulty(aiResult.get("difficulty") instanceof String s ? s : difficulty);
        result.setDurationWeeks(aiDurationWeeks);
        result.setSessionsPerWeek(aiSessionsPerWeek);
        result.setEstimatedDailyCalories(dailyCalories);
        result.setExercises(exerciseDtos);
        return result;
    }

    @SuppressWarnings("unchecked")
    private WorkoutPlanDto mapToPlanDto(WorkoutPlan plan, List<ExerciseDto> exerciseDtos) {
        WorkoutPlanDto dto = new WorkoutPlanDto();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setGoal(plan.getGoal() != null ? plan.getGoal().name() : null);
        dto.setDifficulty(plan.getDifficulty());
        dto.setDurationWeeks(plan.getDurationWeeks());
        dto.setSessionsPerWeek(plan.getSessionsPerWeek());
        dto.setEstimatedDailyCalories(plan.getEstimatedDailyCalories());
        if (plan.getTrainer() != null) {
            dto.setTrainerName(plan.getTrainer().getRealUsername());
        }
        dto.setExercises(exerciseDtos);
        return dto;
    }

    private ExerciseDto mapToExerciseDto(WorkoutExercise ex) {
        ExerciseDto dto = new ExerciseDto();
        dto.setId(ex.getId());
        dto.setExerciseName(ex.getExerciseName());
        dto.setDescription(ex.getDescription());
        dto.setInstructions(ex.getInstructions());
        dto.setSets(ex.getSets());
        dto.setReps(ex.getReps());
        dto.setDurationSeconds(ex.getDurationSeconds());
        dto.setRestSeconds(ex.getRestSeconds());
        dto.setMuscleGroup(ex.getMuscleGroup());
        dto.setEquipment(ex.getEquipment());
        dto.setOrderIndex(ex.getOrderIndex());
        return dto;
    }

    private WorkoutExercise mapFromExerciseDto(ExerciseDto dto) {
        WorkoutExercise ex = new WorkoutExercise();
        ex.setExerciseName(dto.getExerciseName());
        ex.setDescription(dto.getDescription());
        ex.setInstructions(dto.getInstructions());
        ex.setSets(dto.getSets());
        ex.setReps(dto.getReps());
        ex.setDurationSeconds(dto.getDurationSeconds());
        ex.setRestSeconds(dto.getRestSeconds());
        ex.setMuscleGroup(dto.getMuscleGroup());
        ex.setEquipment(dto.getEquipment());
        ex.setOrderIndex(dto.getOrderIndex());
        return ex;
    }
}
