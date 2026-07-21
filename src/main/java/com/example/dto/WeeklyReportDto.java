package com.example.dto;

import java.time.LocalDate;
import java.util.List;

public class WeeklyReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysTracked;

    // Total for the week
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;

    // Daily averages
    private double avgDailyCalories;
    private double avgDailyProtein;
    private double avgDailyCarbs;
    private double avgDailyFat;

    // Goal averages
    private Integer avgCalorieGoal;
    private Integer avgProteinGoal;
    private Integer avgCarbsGoal;
    private Integer avgFatGoal;

    // Average percentages (how close to goals)
    private double avgCaloriePercent;
    private double avgProteinPercent;
    private double avgCarbsPercent;
    private double avgFatPercent;

    // Best day
    private int bestCalorieDay;
    private String bestCalorieDayDate;

    // Macro split (% of calories from each)
    private double proteinCaloriePercent;
    private double carbsCaloriePercent;
    private double fatCaloriePercent;

    // Day-by-day breakdown
    private List<DailySummary> dailySummaries;

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getDaysTracked() { return daysTracked; }
    public void setDaysTracked(int daysTracked) { this.daysTracked = daysTracked; }

    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }

    public double getAvgDailyCalories() { return avgDailyCalories; }
    public void setAvgDailyCalories(double avgDailyCalories) { this.avgDailyCalories = avgDailyCalories; }

    public double getAvgDailyProtein() { return avgDailyProtein; }
    public void setAvgDailyProtein(double avgDailyProtein) { this.avgDailyProtein = avgDailyProtein; }

    public double getAvgDailyCarbs() { return avgDailyCarbs; }
    public void setAvgDailyCarbs(double avgDailyCarbs) { this.avgDailyCarbs = avgDailyCarbs; }

    public double getAvgDailyFat() { return avgDailyFat; }
    public void setAvgDailyFat(double avgDailyFat) { this.avgDailyFat = avgDailyFat; }

    public Integer getAvgCalorieGoal() { return avgCalorieGoal; }
    public void setAvgCalorieGoal(Integer avgCalorieGoal) { this.avgCalorieGoal = avgCalorieGoal; }

    public Integer getAvgProteinGoal() { return avgProteinGoal; }
    public void setAvgProteinGoal(Integer avgProteinGoal) { this.avgProteinGoal = avgProteinGoal; }

    public Integer getAvgCarbsGoal() { return avgCarbsGoal; }
    public void setAvgCarbsGoal(Integer avgCarbsGoal) { this.avgCarbsGoal = avgCarbsGoal; }

    public Integer getAvgFatGoal() { return avgFatGoal; }
    public void setAvgFatGoal(Integer avgFatGoal) { this.avgFatGoal = avgFatGoal; }

    public double getAvgCaloriePercent() { return avgCaloriePercent; }
    public void setAvgCaloriePercent(double avgCaloriePercent) { this.avgCaloriePercent = avgCaloriePercent; }

    public double getAvgProteinPercent() { return avgProteinPercent; }
    public void setAvgProteinPercent(double avgProteinPercent) { this.avgProteinPercent = avgProteinPercent; }

    public double getAvgCarbsPercent() { return avgCarbsPercent; }
    public void setAvgCarbsPercent(double avgCarbsPercent) { this.avgCarbsPercent = avgCarbsPercent; }

    public double getAvgFatPercent() { return avgFatPercent; }
    public void setAvgFatPercent(double avgFatPercent) { this.avgFatPercent = avgFatPercent; }

    public int getBestCalorieDay() { return bestCalorieDay; }
    public void setBestCalorieDay(int bestCalorieDay) { this.bestCalorieDay = bestCalorieDay; }

    public String getBestCalorieDayDate() { return bestCalorieDayDate; }
    public void setBestCalorieDayDate(String bestCalorieDayDate) { this.bestCalorieDayDate = bestCalorieDayDate; }

    public double getProteinCaloriePercent() { return proteinCaloriePercent; }
    public void setProteinCaloriePercent(double proteinCaloriePercent) { this.proteinCaloriePercent = proteinCaloriePercent; }

    public double getCarbsCaloriePercent() { return carbsCaloriePercent; }
    public void setCarbsCaloriePercent(double carbsCaloriePercent) { this.carbsCaloriePercent = carbsCaloriePercent; }

    public double getFatCaloriePercent() { return fatCaloriePercent; }
    public void setFatCaloriePercent(double fatCaloriePercent) { this.fatCaloriePercent = fatCaloriePercent; }

    public List<DailySummary> getDailySummaries() { return dailySummaries; }
    public void setDailySummaries(List<DailySummary> dailySummaries) { this.dailySummaries = dailySummaries; }

    public static class DailySummary {
        private LocalDate date;
        private int calories;
        private double protein;
        private double carbs;
        private double fat;
        private double caloriePercent;
        private int mealCount;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }

        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }

        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }

        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }

        public double getCaloriePercent() { return caloriePercent; }
        public void setCaloriePercent(double caloriePercent) { this.caloriePercent = caloriePercent; }

        public int getMealCount() { return mealCount; }
        public void setMealCount(int mealCount) { this.mealCount = mealCount; }
    }
}
