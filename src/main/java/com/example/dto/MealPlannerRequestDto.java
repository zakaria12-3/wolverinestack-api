package com.example.dto;

import java.time.LocalDate;

public class MealPlannerRequestDto {
    private LocalDate planDate;
    private String mealType;
    private String foodName;
    private String description;
    private Double quantity;
    private String unit;
    private Integer estimatedCalories;
    private Double estimatedProtein;
    private Double estimatedCarbs;
    private Double estimatedFat;
    private Integer orderIndex;

    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getEstimatedCalories() { return estimatedCalories; }
    public void setEstimatedCalories(Integer estimatedCalories) { this.estimatedCalories = estimatedCalories; }

    public Double getEstimatedProtein() { return estimatedProtein; }
    public void setEstimatedProtein(Double estimatedProtein) { this.estimatedProtein = estimatedProtein; }

    public Double getEstimatedCarbs() { return estimatedCarbs; }
    public void setEstimatedCarbs(Double estimatedCarbs) { this.estimatedCarbs = estimatedCarbs; }

    public Double getEstimatedFat() { return estimatedFat; }
    public void setEstimatedFat(Double estimatedFat) { this.estimatedFat = estimatedFat; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
