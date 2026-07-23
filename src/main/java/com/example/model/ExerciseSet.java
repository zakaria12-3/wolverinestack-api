package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_sets")
public class ExerciseSet {

    public enum SetType {
        WARMUP,
        NORMAL,
        FAILURE,
        DROPSET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_exercise_id", nullable = false)
    private WorkoutSessionExercise sessionExercise;

    @Column(nullable = false)
    private Integer setIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SetType setType = SetType.NORMAL;

    private Double weightKg;

    private Integer reps;

    /** Rating of Perceived Exertion (1–10) */
    private Double rpe;

    /** For timed exercises (seconds) */
    private Integer durationSeconds;

    /** For distance-based exercises (meters) */
    private Integer distanceMeters;

    @Column(length = 500)
    private String notes;

    private LocalDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) loggedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkoutSessionExercise getSessionExercise() { return sessionExercise; }
    public void setSessionExercise(WorkoutSessionExercise sessionExercise) { this.sessionExercise = sessionExercise; }

    public Integer getSetIndex() { return setIndex; }
    public void setSetIndex(Integer setIndex) { this.setIndex = setIndex; }

    public SetType getSetType() { return setType; }
    public void setSetType(SetType setType) { this.setType = setType; }

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
}
