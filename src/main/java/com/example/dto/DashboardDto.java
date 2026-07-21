package com.example.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardDto {

    // 1. User's current goals from profile
    private ProfileSection profile;

    // 2. Today's nutrition progress vs goals
    private NutritionSection nutrition;

    // 3. Latest body measurement
    private MeasurementSection measurement;

    // 4. Recent workout session stats
    private WorkoutSection workout;

    // 5. AI-generated coach note
    private CoachNote coachNote;

    public ProfileSection getProfile() { return profile; }
    public void setProfile(ProfileSection profile) { this.profile = profile; }

    public NutritionSection getNutrition() { return nutrition; }
    public void setNutrition(NutritionSection nutrition) { this.nutrition = nutrition; }

    public MeasurementSection getMeasurement() { return measurement; }
    public void setMeasurement(MeasurementSection measurement) { this.measurement = measurement; }

    public WorkoutSection getWorkout() { return workout; }
    public void setWorkout(WorkoutSection workout) { this.workout = workout; }

    public CoachNote getCoachNote() { return coachNote; }
    public void setCoachNote(CoachNote coachNote) { this.coachNote = coachNote; }

    // --- Inner sections ---

    public static class ProfileSection {
        private String username;
        private String email;
        private String fitnessGoal;
        private String activityLevel;
        private String gender;
        private Double weightKg;
        private Double heightCm;
        private Integer dailyCalorieGoal;
        private Integer dailyProteinGoal;
        private Integer dailyCarbsGoal;
        private Integer dailyFatGoal;
        private boolean onboardingComplete;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFitnessGoal() { return fitnessGoal; }
        public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }
        public String getActivityLevel() { return activityLevel; }
        public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
        public Double getHeightCm() { return heightCm; }
        public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
        public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
        public void setDailyCalorieGoal(Integer dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }
        public Integer getDailyProteinGoal() { return dailyProteinGoal; }
        public void setDailyProteinGoal(Integer dailyProteinGoal) { this.dailyProteinGoal = dailyProteinGoal; }
        public Integer getDailyCarbsGoal() { return dailyCarbsGoal; }
        public void setDailyCarbsGoal(Integer dailyCarbsGoal) { this.dailyCarbsGoal = dailyCarbsGoal; }
        public Integer getDailyFatGoal() { return dailyFatGoal; }
        public void setDailyFatGoal(Integer dailyFatGoal) { this.dailyFatGoal = dailyFatGoal; }
        public boolean isOnboardingComplete() { return onboardingComplete; }
        public void setOnboardingComplete(boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }
    }

    public static class NutritionSection {
        private LocalDate date;
        private int totalCalories;
        private double totalProtein;
        private double totalCarbs;
        private double totalFat;
        private int mealCount;
        private Integer calorieGoal;
        private Integer proteinGoal;
        private Integer carbsGoal;
        private Integer fatGoal;
        private double caloriePercent;
        private double proteinPercent;
        private double carbsPercent;
        private double fatPercent;
        private int caloriesRemaining;
        private double proteinRemaining;
        private double carbsRemaining;
        private double fatRemaining;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public int getTotalCalories() { return totalCalories; }
        public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
        public double getTotalProtein() { return totalProtein; }
        public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }
        public double getTotalCarbs() { return totalCarbs; }
        public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }
        public double getTotalFat() { return totalFat; }
        public void setTotalFat(double totalFat) { this.totalFat = totalFat; }
        public int getMealCount() { return mealCount; }
        public void setMealCount(int mealCount) { this.mealCount = mealCount; }
        public Integer getCalorieGoal() { return calorieGoal; }
        public void setCalorieGoal(Integer calorieGoal) { this.calorieGoal = calorieGoal; }
        public Integer getProteinGoal() { return proteinGoal; }
        public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }
        public Integer getCarbsGoal() { return carbsGoal; }
        public void setCarbsGoal(Integer carbsGoal) { this.carbsGoal = carbsGoal; }
        public Integer getFatGoal() { return fatGoal; }
        public void setFatGoal(Integer fatGoal) { this.fatGoal = fatGoal; }
        public double getCaloriePercent() { return caloriePercent; }
        public void setCaloriePercent(double caloriePercent) { this.caloriePercent = caloriePercent; }
        public double getProteinPercent() { return proteinPercent; }
        public void setProteinPercent(double proteinPercent) { this.proteinPercent = proteinPercent; }
        public double getCarbsPercent() { return carbsPercent; }
        public void setCarbsPercent(double carbsPercent) { this.carbsPercent = carbsPercent; }
        public double getFatPercent() { return fatPercent; }
        public void setFatPercent(double fatPercent) { this.fatPercent = fatPercent; }
        public int getCaloriesRemaining() { return caloriesRemaining; }
        public void setCaloriesRemaining(int caloriesRemaining) { this.caloriesRemaining = caloriesRemaining; }
        public double getProteinRemaining() { return proteinRemaining; }
        public void setProteinRemaining(double proteinRemaining) { this.proteinRemaining = proteinRemaining; }
        public double getCarbsRemaining() { return carbsRemaining; }
        public void setCarbsRemaining(double carbsRemaining) { this.carbsRemaining = carbsRemaining; }
        public double getFatRemaining() { return fatRemaining; }
        public void setFatRemaining(double fatRemaining) { this.fatRemaining = fatRemaining; }
    }

    public static class MeasurementSection {
        private boolean hasData;
        private LocalDate latestDate;
        private Double weightKg;
        private Double bodyFatPercent;
        private Double waistCm;
        private Double chestCm;
        private Double weightChangeKg;
        private Double bodyFatChange;

        public boolean isHasData() { return hasData; }
        public void setHasData(boolean hasData) { this.hasData = hasData; }
        public LocalDate getLatestDate() { return latestDate; }
        public void setLatestDate(LocalDate latestDate) { this.latestDate = latestDate; }
        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
        public Double getBodyFatPercent() { return bodyFatPercent; }
        public void setBodyFatPercent(Double bodyFatPercent) { this.bodyFatPercent = bodyFatPercent; }
        public Double getWaistCm() { return waistCm; }
        public void setWaistCm(Double waistCm) { this.waistCm = waistCm; }
        public Double getChestCm() { return chestCm; }
        public void setChestCm(Double chestCm) { this.chestCm = chestCm; }
        public Double getWeightChangeKg() { return weightChangeKg; }
        public void setWeightChangeKg(Double weightChangeKg) { this.weightChangeKg = weightChangeKg; }
        public Double getBodyFatChange() { return bodyFatChange; }
        public void setBodyFatChange(Double bodyFatChange) { this.bodyFatChange = bodyFatChange; }
    }

    public static class WorkoutSection {
        private long totalSessions;
        private long sessionsThisWeek;
        private int totalMinutesThisWeek;
        private int totalCaloriesBurnedThisWeek;
        private String currentPlanName;
        private List<WorkoutSessionDto> recentSessions;

        public long getTotalSessions() { return totalSessions; }
        public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }
        public long getSessionsThisWeek() { return sessionsThisWeek; }
        public void setSessionsThisWeek(long sessionsThisWeek) { this.sessionsThisWeek = sessionsThisWeek; }
        public int getTotalMinutesThisWeek() { return totalMinutesThisWeek; }
        public void setTotalMinutesThisWeek(int totalMinutesThisWeek) { this.totalMinutesThisWeek = totalMinutesThisWeek; }
        public int getTotalCaloriesBurnedThisWeek() { return totalCaloriesBurnedThisWeek; }
        public void setTotalCaloriesBurnedThisWeek(int totalCaloriesBurnedThisWeek) { this.totalCaloriesBurnedThisWeek = totalCaloriesBurnedThisWeek; }
        public String getCurrentPlanName() { return currentPlanName; }
        public void setCurrentPlanName(String currentPlanName) { this.currentPlanName = currentPlanName; }
        public List<WorkoutSessionDto> getRecentSessions() { return recentSessions; }
        public void setRecentSessions(List<WorkoutSessionDto> recentSessions) { this.recentSessions = recentSessions; }
    }

    public static class CoachNote {
        private String message;
        private String focus;
        private String suggestion;

        public CoachNote() {}

        public CoachNote(String message, String focus, String suggestion) {
            this.message = message;
            this.focus = focus;
            this.suggestion = suggestion;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getFocus() { return focus; }
        public void setFocus(String focus) { this.focus = focus; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
