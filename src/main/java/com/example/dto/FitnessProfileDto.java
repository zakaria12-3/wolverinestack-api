package com.example.dto;

import java.time.LocalDate;

public class FitnessProfileDto {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String headline;
    private String location;
    private String avatarUrl;
    private String phone;

    // Fitness fields
    private String gender;
    private Double weightKg;
    private Double heightCm;
    private LocalDate dateOfBirth;
    private String fitnessGoal;
    private String activityLevel;
    private Integer dailyCalorieGoal;
    private Integer dailyProteinGoal;
    private Integer dailyCarbsGoal;
    private Integer dailyFatGoal;

    private String gymName;
    private Boolean dailyTipEnabled = false;
    private Boolean reported = false;
    private Boolean suspended = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }

    public Integer getDailyProteinGoal() { return dailyProteinGoal; }
    public void setDailyProteinGoal(Integer dailyProteinGoal) { this.dailyProteinGoal = dailyProteinGoal; }

    public Integer getDailyCarbsGoal() { return dailyCarbsGoal; }
    public void setDailyCarbsGoal(Integer dailyCarbsGoal) { this.dailyCarbsGoal = dailyCarbsGoal; }

    public Integer getDailyFatGoal() { return dailyFatGoal; }
    public void setDailyFatGoal(Integer dailyFatGoal) { this.dailyFatGoal = dailyFatGoal; }

    public String getGymName() { return gymName; }
    public void setGymName(String gymName) { this.gymName = gymName; }

    public Boolean getDailyTipEnabled() { return dailyTipEnabled; }
    public void setDailyTipEnabled(Boolean dailyTipEnabled) { this.dailyTipEnabled = dailyTipEnabled; }

    public Boolean getReported() { return reported; }
    public void setReported(Boolean reported) { this.reported = reported; }

    public Boolean getSuspended() { return suspended; }
    public void setSuspended(Boolean suspended) { this.suspended = suspended; }
}
