package com.example.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 2000)
    private String description;

    @Column(length = 3000)
    private String instructions;

    private String muscleGroup;       // chest, back, legs, shoulders, arms, core, full_body
    private String category;          // strength, cardio, hiit, flexibility, bodyweight
    private String difficulty;        // beginner, intermediate, advanced
    private String equipment;         // none, dumbbells, barbell, kettlebells, machine, bands

    private Integer estimatedCaloriesBurned;
    private Integer estimatedDurationMinutes;
    private Integer sets;
    private Integer reps;

    private String videoUrl;
    private String imageUrl;

    private LocalDateTime createdAt;
    private Boolean active = true;

    private Integer riskScore = 0;
    private Boolean suspicious = false;
    private String moderationStatus = "APPROVED";

    @Column(length = 1000)
    private String moderationReason;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private User trainer;

    public Workout() {
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public Integer getEstimatedCaloriesBurned() { return estimatedCaloriesBurned; }
    public void setEstimatedCaloriesBurned(Integer estimatedCaloriesBurned) { this.estimatedCaloriesBurned = estimatedCaloriesBurned; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getActive() { return active == null || active; }
    public void setActive(Boolean active) { this.active = active; }

    public User getTrainer() { return trainer; }
    public void setTrainer(User trainer) { this.trainer = trainer; }

    public int getRiskScore() { return riskScore == null ? 0 : riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public boolean isSuspicious() { return suspicious != null && suspicious; }
    public void setSuspicious(Boolean suspicious) { this.suspicious = suspicious; }

    public String getModerationStatus() { return moderationStatus == null ? "APPROVED" : moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public String getModerationReason() { return moderationReason; }
    public void setModerationReason(String moderationReason) { this.moderationReason = moderationReason; }
}
