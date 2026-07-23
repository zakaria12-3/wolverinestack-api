package com.example.dto;

public class LogSetRequestDto {
    private String setType;       // WARMUP, NORMAL, FAILURE, DROPSET
    private Double weightKg;
    private Integer reps;
    private Double rpe;
    private Integer durationSeconds;
    private Integer distanceMeters;
    private String notes;

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
}
