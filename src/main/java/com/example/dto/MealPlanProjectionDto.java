package com.example.dto;

import java.time.LocalDate;
import java.util.List;

public class MealPlanProjectionDto {
    private LocalDate date;
    private List<PlannedMealDto> plannedMeals;
    private int mealCount;

    // Projected totals
    private int projectedCalories;
    private double projectedProtein;
    private double projectedCarbs;
    private double projectedFat;

    // Goals
    private Integer calorieGoal;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatGoal;

    // Remaining after planned meals
    private int remainingCalories;
    private double remainingProtein;
    private double remainingCarbs;
    private double remainingFat;

    // Percentages of goal achieved by planned meals
    private double caloriePercent;
    private double proteinPercent;
    private double carbsPercent;
    private double fatPercent;

    // AI suggestions for filling remaining macros
    private List<AISuggestion> aiSuggestions;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<PlannedMealDto> getPlannedMeals() { return plannedMeals; }
    public void setPlannedMeals(List<PlannedMealDto> plannedMeals) { this.plannedMeals = plannedMeals; }

    public int getMealCount() { return mealCount; }
    public void setMealCount(int mealCount) { this.mealCount = mealCount; }

    public int getProjectedCalories() { return projectedCalories; }
    public void setProjectedCalories(int projectedCalories) { this.projectedCalories = projectedCalories; }

    public double getProjectedProtein() { return projectedProtein; }
    public void setProjectedProtein(double projectedProtein) { this.projectedProtein = projectedProtein; }

    public double getProjectedCarbs() { return projectedCarbs; }
    public void setProjectedCarbs(double projectedCarbs) { this.projectedCarbs = projectedCarbs; }

    public double getProjectedFat() { return projectedFat; }
    public void setProjectedFat(double projectedFat) { this.projectedFat = projectedFat; }

    public Integer getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(Integer calorieGoal) { this.calorieGoal = calorieGoal; }

    public Integer getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(Integer proteinGoal) { this.proteinGoal = proteinGoal; }

    public Integer getCarbsGoal() { return carbsGoal; }
    public void setCarbsGoal(Integer carbsGoal) { this.carbsGoal = carbsGoal; }

    public Integer getFatGoal() { return fatGoal; }
    public void setFatGoal(Integer fatGoal) { this.fatGoal = fatGoal; }

    public int getRemainingCalories() { return remainingCalories; }
    public void setRemainingCalories(int remainingCalories) { this.remainingCalories = remainingCalories; }

    public double getRemainingProtein() { return remainingProtein; }
    public void setRemainingProtein(double remainingProtein) { this.remainingProtein = remainingProtein; }

    public double getRemainingCarbs() { return remainingCarbs; }
    public void setRemainingCarbs(double remainingCarbs) { this.remainingCarbs = remainingCarbs; }

    public double getRemainingFat() { return remainingFat; }
    public void setRemainingFat(double remainingFat) { this.remainingFat = remainingFat; }

    public double getCaloriePercent() { return caloriePercent; }
    public void setCaloriePercent(double caloriePercent) { this.caloriePercent = caloriePercent; }

    public double getProteinPercent() { return proteinPercent; }
    public void setProteinPercent(double proteinPercent) { this.proteinPercent = proteinPercent; }

    public double getCarbsPercent() { return carbsPercent; }
    public void setCarbsPercent(double carbsPercent) { this.carbsPercent = carbsPercent; }

    public double getFatPercent() { return fatPercent; }
    public void setFatPercent(double fatPercent) { this.fatPercent = fatPercent; }

    public List<AISuggestion> getAiSuggestions() { return aiSuggestions; }
    public void setAiSuggestions(List<AISuggestion> aiSuggestions) { this.aiSuggestions = aiSuggestions; }

    public static class AISuggestion {
        private String suggestion;
        private String mealType;
        private int estimatedCalories;
        private double estimatedProtein;
        private double estimatedCarbs;
        private double estimatedFat;
        private String reasoning;

        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

        public String getMealType() { return mealType; }
        public void setMealType(String mealType) { this.mealType = mealType; }

        public int getEstimatedCalories() { return estimatedCalories; }
        public void setEstimatedCalories(int estimatedCalories) { this.estimatedCalories = estimatedCalories; }

        public double getEstimatedProtein() { return estimatedProtein; }
        public void setEstimatedProtein(double estimatedProtein) { this.estimatedProtein = estimatedProtein; }

        public double getEstimatedCarbs() { return estimatedCarbs; }
        public void setEstimatedCarbs(double estimatedCarbs) { this.estimatedCarbs = estimatedCarbs; }

        public double getEstimatedFat() { return estimatedFat; }
        public void setEstimatedFat(double estimatedFat) { this.estimatedFat = estimatedFat; }

        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    }
}
