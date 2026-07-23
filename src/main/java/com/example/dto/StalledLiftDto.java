package com.example.dto;

import java.time.LocalDate;

public class StalledLiftDto {

    private String exerciseName;
    private String muscleGroup;
    private String equipment;
    private Double bestWeightKg;
    private Integer bestReps;
    private Integer estimatedOneRm;
    private LocalDate bestAchievedDate;
    private long daysSinceImprovement;
    private int totalSessionsLogged;
    private int totalSetsLogged;
    private String aiSuggestion;

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public Double getBestWeightKg() { return bestWeightKg; }
    public void setBestWeightKg(Double bestWeightKg) { this.bestWeightKg = bestWeightKg; }

    public Integer getBestReps() { return bestReps; }
    public void setBestReps(Integer bestReps) { this.bestReps = bestReps; }

    public Integer getEstimatedOneRm() { return estimatedOneRm; }
    public void setEstimatedOneRm(Integer estimatedOneRm) { this.estimatedOneRm = estimatedOneRm; }

    public LocalDate getBestAchievedDate() { return bestAchievedDate; }
    public void setBestAchievedDate(LocalDate bestAchievedDate) { this.bestAchievedDate = bestAchievedDate; }

    public long getDaysSinceImprovement() { return daysSinceImprovement; }
    public void setDaysSinceImprovement(long daysSinceImprovement) { this.daysSinceImprovement = daysSinceImprovement; }

    public int getTotalSessionsLogged() { return totalSessionsLogged; }
    public void setTotalSessionsLogged(int totalSessionsLogged) { this.totalSessionsLogged = totalSessionsLogged; }

    public int getTotalSetsLogged() { return totalSetsLogged; }
    public void setTotalSetsLogged(int totalSetsLogged) { this.totalSetsLogged = totalSetsLogged; }

    public String getAiSuggestion() { return aiSuggestion; }
    public void setAiSuggestion(String aiSuggestion) { this.aiSuggestion = aiSuggestion; }
}
