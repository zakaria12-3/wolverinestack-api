package com.example.dto;

import java.time.LocalDate;
import java.util.List;

public class BodyMeasurementProgressDto {
    private List<MeasurementPoint> measurements;

    // Combined nutrition data for the same period (optional, for chart overlays)
    private List<NutritionPoint> dailyCalories;
    private List<NutritionPoint> dailyProtein;

    public List<MeasurementPoint> getMeasurements() { return measurements; }
    public void setMeasurements(List<MeasurementPoint> measurements) { this.measurements = measurements; }

    public List<NutritionPoint> getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(List<NutritionPoint> dailyCalories) { this.dailyCalories = dailyCalories; }

    public List<NutritionPoint> getDailyProtein() { return dailyProtein; }
    public void setDailyProtein(List<NutritionPoint> dailyProtein) { this.dailyProtein = dailyProtein; }

    public static class MeasurementPoint {
        private LocalDate date;
        private Double weightKg;
        private Double bodyFatPercent;
        private Double waistCm;
        private Double chestCm;
        private Double leftArmCm;
        private Double rightArmCm;
        private Double leftThighCm;
        private Double rightThighCm;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

        public Double getBodyFatPercent() { return bodyFatPercent; }
        public void setBodyFatPercent(Double bodyFatPercent) { this.bodyFatPercent = bodyFatPercent; }

        public Double getWaistCm() { return waistCm; }
        public void setWaistCm(Double waistCm) { this.waistCm = waistCm; }

        public Double getChestCm() { return chestCm; }
        public void setChestCm(Double chestCm) { this.chestCm = chestCm; }

        public Double getLeftArmCm() { return leftArmCm; }
        public void setLeftArmCm(Double leftArmCm) { this.leftArmCm = leftArmCm; }

        public Double getRightArmCm() { return rightArmCm; }
        public void setRightArmCm(Double rightArmCm) { this.rightArmCm = rightArmCm; }

        public Double getLeftThighCm() { return leftThighCm; }
        public void setLeftThighCm(Double leftThighCm) { this.leftThighCm = leftThighCm; }

        public Double getRightThighCm() { return rightThighCm; }
        public void setRightThighCm(Double rightThighCm) { this.rightThighCm = rightThighCm; }
    }

    public static class NutritionPoint {
        private LocalDate date;
        private double value;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
    }
}
