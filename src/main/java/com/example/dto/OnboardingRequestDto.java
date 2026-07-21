package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class OnboardingRequestDto {

    @NotNull(message = "Gender is required")
    private String gender;

    @NotNull(message = "Fitness goal is required")
    private String fitnessGoal;

    @NotNull(message = "Activity level is required")
    private String activityLevel;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weightKg;

    @NotNull(message = "Height is required")
    @Positive(message = "Height must be positive")
    private Double heightCm;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private boolean calculateTdee = true;

    // Optionally override auto-calculated goals
    private Integer dailyCalorieGoal;
    private Integer dailyProteinGoal;
    private Integer dailyCarbsGoal;
    private Integer dailyFatGoal;

    // Getters and Setters

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public boolean isCalculateTdee() { return calculateTdee; }
    public void setCalculateTdee(boolean calculateTdee) { this.calculateTdee = calculateTdee; }

    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }

    public Integer getDailyProteinGoal() { return dailyProteinGoal; }
    public void setDailyProteinGoal(Integer dailyProteinGoal) { this.dailyProteinGoal = dailyProteinGoal; }

    public Integer getDailyCarbsGoal() { return dailyCarbsGoal; }
    public void setDailyCarbsGoal(Integer dailyCarbsGoal) { this.dailyCarbsGoal = dailyCarbsGoal; }

    public Integer getDailyFatGoal() { return dailyFatGoal; }
    public void setDailyFatGoal(Integer dailyFatGoal) { this.dailyFatGoal = dailyFatGoal; }
}
