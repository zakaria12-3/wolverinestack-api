package com.example.dto;

import java.util.List;

public class AIWorkoutSuggestionDto {
    private String suggestedWorkout;
    private String muscleGroup;
    private String difficulty;
    private Integer estimatedCaloriesBurn;
    private Integer durationMinutes;
    private String reasoning;
    private List<String> alternativeExercises;
    private String nutritionTip;

    public String getSuggestedWorkout() { return suggestedWorkout; }
    public void setSuggestedWorkout(String suggestedWorkout) { this.suggestedWorkout = suggestedWorkout; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getEstimatedCaloriesBurn() { return estimatedCaloriesBurn; }
    public void setEstimatedCaloriesBurn(Integer estimatedCaloriesBurn) { this.estimatedCaloriesBurn = estimatedCaloriesBurn; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public List<String> getAlternativeExercises() { return alternativeExercises; }
    public void setAlternativeExercises(List<String> alternativeExercises) { this.alternativeExercises = alternativeExercises; }

    public String getNutritionTip() { return nutritionTip; }
    public void setNutritionTip(String nutritionTip) { this.nutritionTip = nutritionTip; }
}
