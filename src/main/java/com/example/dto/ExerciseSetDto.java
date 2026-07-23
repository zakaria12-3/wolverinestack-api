package com.example.dto;

import java.time.LocalDateTime;

public class ExerciseSetDto {

    private Long id;
    private Integer setIndex;
    private String setType;        // WARMUP, NORMAL, FAILURE, DROPSET
    private Double weightKg;
    private Integer reps;
    private Double rpe;
    private Integer durationSeconds;
    private Integer distanceMeters;
    private String notes;
    private LocalDateTime loggedAt;

    // Previous best for PR comparison
    private Double previousBestWeight;
    private Integer previousBestReps;
    private Boolean isPersonalRecord;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSetIndex() { return setIndex; }
    public void setSetIndex(Integer setIndex) { this.setIndex = setIndex; }

    public String getSetType() { return setType; }
    public void setSetType(String setType) { this.setType = setType; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Double getRpe() { return rpe; }
    public void setRpe(Double rpe) { this.rpe = rpe; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Integer distanceMeters) { this.distanceMeters = distanceMeters; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    public Double getPreviousBestWeight() { return previousBestWeight; }
    public void setPreviousBestWeight(Double previousBestWeight) { this.previousBestWeight = previousBestWeight; }

    public Integer getPreviousBestReps() { return previousBestReps; }
    public void setPreviousBestReps(Integer previousBestReps) { this.previousBestReps = previousBestReps; }

    public Boolean getIsPersonalRecord() { return isPersonalRecord; }
    public void setIsPersonalRecord(Boolean isPersonalRecord) { this.isPersonalRecord = isPersonalRecord; }
}
