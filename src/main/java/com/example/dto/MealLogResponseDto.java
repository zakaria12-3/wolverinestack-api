package com.example.dto;

public class MealLogResponseDto {
    private Long mealEntryId;
    private String foodName;
    private Integer estimatedCalories;
    private boolean aiAnalyzed;

    public MealLogResponseDto() {}

    public MealLogResponseDto(Long mealEntryId, String foodName, Integer estimatedCalories, boolean aiAnalyzed) {
        this.mealEntryId = mealEntryId;
        this.foodName = foodName;
        this.estimatedCalories = estimatedCalories;
        this.aiAnalyzed = aiAnalyzed;
    }

    public Long getMealEntryId() { return mealEntryId; }
    public boolean isAiAnalyzed() { return aiAnalyzed; }
    public String getFoodName() { return foodName; }
    public Integer getEstimatedCalories() { return estimatedCalories; }
}
