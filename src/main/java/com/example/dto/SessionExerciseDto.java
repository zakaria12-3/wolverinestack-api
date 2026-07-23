package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SessionExerciseDto {
    private Long id;
    private String exerciseName;
    private String muscleGroup;
    private String equipment;
    private String imageUrl;

    // Target values (from plan)
    private Integer targetSets;
    private Integer targetReps;

    // Derived from sets
    private int completedSetCount;
    private int totalReps;
    private double totalVolume;

    private Integer durationSeconds;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;
    private LocalDateTime loggedAt;

    private List<ExerciseSetDto> sets;

    // Previous best for PR indicator
    private Double previousBestWeight;
    private Integer previousBestReps;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getTargetSets() { return targetSets; }
    public void setTargetSets(Integer targetSets) { this.targetSets = targetSets; }

    public Integer getTargetReps() { return targetReps; }
    public void setTargetReps(Integer targetReps) { this.targetReps = targetReps; }

    public int getCompletedSetCount() { return completedSetCount; }
    public void setCompletedSetCount(int completedSetCount) { this.completedSetCount = completedSetCount; }

    public int getTotalReps() { return totalReps; }
    public void setTotalReps(int totalReps) { this.totalReps = totalReps; }

    public double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getRestSeconds() { return restSeconds; }
    public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public List<ExerciseSetDto> getSets() { return sets; }
    public void setSets(List<ExerciseSetDto> sets) { this.sets = sets; }

    public Double getPreviousBestWeight() { return previousBestWeight; }
    public void setPreviousBestWeight(Double previousBestWeight) { this.previousBestWeight = previousBestWeight; }

    public Integer getPreviousBestReps() { return previousBestReps; }
    public void setPreviousBestReps(Integer previousBestReps) { this.previousBestReps = previousBestReps; }
}
