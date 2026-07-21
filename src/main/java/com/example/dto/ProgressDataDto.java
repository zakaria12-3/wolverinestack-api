package com.example.dto;

import java.time.LocalDate;
import java.util.List;

public class ProgressDataDto {
    private String exerciseName;
    private List<ProgressPoint> progressPoints;

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public List<ProgressPoint> getProgressPoints() { return progressPoints; }
    public void setProgressPoints(List<ProgressPoint> progressPoints) { this.progressPoints = progressPoints; }

    public static class ProgressPoint {
        private LocalDate date;
        private Double weightKg;
        private Integer reps;
        private Integer sets;
        private Integer estimatedOneRm;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

        public Integer getReps() { return reps; }
        public void setReps(Integer reps) { this.reps = reps; }

        public Integer getSets() { return sets; }
        public void setSets(Integer sets) { this.sets = sets; }

        public Integer getEstimatedOneRm() { return estimatedOneRm; }
        public void setEstimatedOneRm(Integer estimatedOneRm) { this.estimatedOneRm = estimatedOneRm; }
    }
}
