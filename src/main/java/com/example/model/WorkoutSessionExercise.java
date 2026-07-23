package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_session_exercises")
public class WorkoutSessionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private WorkoutSession session;

    @Column(nullable = false)
    private String exerciseName;

    private String muscleGroup;
    private String equipment;
    private String imageUrl;

    /** Target values from the plan (guidance only) */
    private Integer targetSets;
    private Integer targetReps;

    /** For timed exercises */
    private Integer durationSeconds;
    private Integer restSeconds;

    private Integer orderIndex;

    @Column(length = 500)
    private String notes;

    private LocalDateTime loggedAt;

    @OneToMany(mappedBy = "sessionExercise", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("setIndex ASC")
    private List<ExerciseSet> sets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) loggedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkoutSession getSession() { return session; }
    public void setSession(WorkoutSession session) { this.session = session; }

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

    public List<ExerciseSet> getSets() { return sets; }
    public void setSets(List<ExerciseSet> sets) { this.sets = sets; }

    // --- Derived / computed fields ---

    /** Total volume (sum of weight × reps across all sets) */
    public double getTotalVolume() {
        return sets.stream()
                .filter(s -> s.getWeightKg() != null && s.getReps() != null)
                .mapToDouble(s -> s.getWeightKg() * s.getReps())
                .sum();
    }

    /** Best set by weight (for PR tracking) */
    public ExerciseSet getBestSetByWeight() {
        return sets.stream()
                .filter(s -> s.getWeightKg() != null && s.getReps() != null)
                .max(java.util.Comparator.comparingDouble(ExerciseSet::getWeightKg))
                .orElse(null);
    }

    /** Best set by 1RM estimate (Epley formula: weight × (1 + reps/30)) */
    public ExerciseSet getBestSetByEstimated1RM() {
        return sets.stream()
                .filter(s -> s.getWeightKg() != null && s.getReps() != null && s.getReps() > 0)
                .max(java.util.Comparator.comparingDouble(
                        s -> s.getWeightKg() * (1 + s.getReps() / 30.0)))
                .orElse(null);
    }

    /** Number of completed sets (checks reps OR duration OR distance) */
    public int getCompletedSetCount() {
        return (int) sets.stream()
                .filter(s -> (s.getReps() != null && s.getReps() > 0)
                          || (s.getDurationSeconds() != null && s.getDurationSeconds() > 0)
                          || (s.getDistanceMeters() != null && s.getDistanceMeters() > 0))
                .count();
    }

    /** Total reps across all sets */
    public int getTotalReps() {
        return sets.stream()
                .filter(s -> s.getReps() != null)
                .mapToInt(ExerciseSet::getReps)
                .sum();
    }
}
