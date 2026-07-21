package com.example.dto;

public class TdeeResultDto {
    private double bmr;
    private double tdee;
    private String activityLevel;
    private String fitnessGoal;

    // Recommended targets based on goal
    private int recommendedCalories;
    private int recommendedProteinGrams;
    private int recommendedCarbsGrams;
    private int recommendedFatGrams;

    public double getBmr() { return bmr; }
    public void setBmr(double bmr) { this.bmr = bmr; }

    public double getTdee() { return tdee; }
    public void setTdee(double tdee) { this.tdee = tdee; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(String fitnessGoal) { this.fitnessGoal = fitnessGoal; }

    public int getRecommendedCalories() { return recommendedCalories; }
    public void setRecommendedCalories(int recommendedCalories) { this.recommendedCalories = recommendedCalories; }

    public int getRecommendedProteinGrams() { return recommendedProteinGrams; }
    public void setRecommendedProteinGrams(int recommendedProteinGrams) { this.recommendedProteinGrams = recommendedProteinGrams; }

    public int getRecommendedCarbsGrams() { return recommendedCarbsGrams; }
    public void setRecommendedCarbsGrams(int recommendedCarbsGrams) { this.recommendedCarbsGrams = recommendedCarbsGrams; }

    public int getRecommendedFatGrams() { return recommendedFatGrams; }
    public void setRecommendedFatGrams(int recommendedFatGrams) { this.recommendedFatGrams = recommendedFatGrams; }
}
