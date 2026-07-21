package com.example.dto;

public class WorkoutSessionResultDto {
    private int caloriesBurned;
    private int totalExercises;
    private int completedExercises;
    private int durationMinutes;
    private boolean completed;
    private String message;

    public WorkoutSessionResultDto() {}

    public WorkoutSessionResultDto(int caloriesBurned, int totalExercises, int completedExercises,
                                   int durationMinutes, boolean completed, String message) {
        this.caloriesBurned = caloriesBurned;
        this.totalExercises = totalExercises;
        this.completedExercises = completedExercises;
        this.durationMinutes = durationMinutes;
        this.completed = completed;
        this.message = message;
    }

    public int getCaloriesBurned() { return caloriesBurned; }
    public int getTotalExercises() { return totalExercises; }
    public int getCompletedExercises() { return completedExercises; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isCompleted() { return completed; }
    public String getMessage() { return message; }
}
