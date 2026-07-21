package com.example.dto;

import java.util.List;

public class CreateWorkoutPlanDto {
    private Long trainerId;
    private String name;
    private String description;
    private String goal;
    private String difficulty;
    private Integer durationWeeks;
    private Integer sessionsPerWeek;
    private Integer estimatedDailyCalories;
    private List<ExerciseDto> exercises;

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }

    public Integer getSessionsPerWeek() { return sessionsPerWeek; }
    public void setSessionsPerWeek(Integer sessionsPerWeek) { this.sessionsPerWeek = sessionsPerWeek; }

    public Integer getEstimatedDailyCalories() { return estimatedDailyCalories; }
    public void setEstimatedDailyCalories(Integer estimatedDailyCalories) { this.estimatedDailyCalories = estimatedDailyCalories; }

    public List<ExerciseDto> getExercises() { return exercises; }
    public void setExercises(List<ExerciseDto> exercises) { this.exercises = exercises; }
}
