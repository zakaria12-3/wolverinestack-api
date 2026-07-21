package com.example.dto;

import java.time.LocalDate;
import java.util.List;

public class DailyProgressDto {
    private LocalDate date;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private int mealCount;

    // Goal targets
    private Integer calorieGoal;
    private Integer proteinGoal;
    private Integer carbsGoal;
    private Integer fatGoal;

    // Percentages (for progress bars)
    private double caloriePercent;
    private double proteinPercent;
    private double carbsPercent;
    private double fatPercent;

    // Remaining
    private int caloriesRemaining;
    private double proteinRemaining;
    private double carbsRemaining;
    private double fatRemaining;

    private List<MealEntryDto> meals;

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

    public List<MealEntryDto> getMeals() { return meals; }
    public void setMeals(List<MealEntryDto> meals) { this.meals = meals; }
}
