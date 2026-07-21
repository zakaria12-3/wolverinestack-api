package com.example.model;

public enum MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    PRE_WORKOUT,
    POST_WORKOUT;

    public static String normalize(String type) {
        if (type == null || type.isBlank()) {
            return SNACK.name();
        }
        return switch (type.trim().toUpperCase()) {
            case "BREAKFAST" -> BREAKFAST.name();
            case "LUNCH" -> LUNCH.name();
            case "DINNER" -> DINNER.name();
            case "SNACK" -> SNACK.name();
            case "PRE-WORKOUT", "PRE_WORKOUT", "PREWORKOUT" -> PRE_WORKOUT.name();
            case "POST-WORKOUT", "POST_WORKOUT", "POSTWORKOUT" -> POST_WORKOUT.name();
            default -> SNACK.name();
        };
    }
}
