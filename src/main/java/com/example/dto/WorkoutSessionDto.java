package com.example.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutSessionDto {
    private Long id;
    private String sessionName;
    private Long planId;
    private String planName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean completed = false;
    private Integer durationMinutes;
    private Integer caloriesBurned;
    private String intensity;
    private String notes;
    private Integer rating;
    private int totalExercises;
    private int completedExercises;
    private List<WorkoutLogEntryDto> logEntries;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(Integer caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getIntensity() { return intensity; }
    public void setIntensity(String intensity) { this.intensity = intensity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public int getTotalExercises() { return totalExercises; }
    public void setTotalExercises(int totalExercises) { this.totalExercises = totalExercises; }

    public int getCompletedExercises() { return completedExercises; }
    public void setCompletedExercises(int completedExercises) { this.completedExercises = completedExercises; }

    public List<WorkoutLogEntryDto> getLogEntries() { return logEntries; }
    public void setLogEntries(List<WorkoutLogEntryDto> logEntries) { this.logEntries = logEntries; }
}
