package com.example.dto;

import java.time.LocalDateTime;

public class WorkoutLogEntryDto {
    private Long id;
    private String exerciseName;
    private String muscleGroup;
    private String equipment;
    private String imageUrl;
    private Integer targetSets;
    private Integer targetReps;
    private Integer actualSets;
    private Integer actualReps;
    private Double weightKg;
    private Integer durationSeconds;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;
    private LocalDateTime loggedAt;

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

    public Integer getActualSets() { return actualSets; }
    public void setActualSets(Integer actualSets) { this.actualSets = actualSets; }

    public Integer getActualReps() { return actualReps; }
    public void setActualReps(Integer actualReps) { this.actualReps = actualReps; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

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
}
