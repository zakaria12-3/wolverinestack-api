package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FitnessAIService {

    private final AIService aiService;

    public FitnessAIService(AIService aiService) {
        this.aiService = aiService;
    }

    public Map<String, Object> analyzeMeal(String foodDescription, String mealType) {
        String prompt = """
        You are a precise nutrition analyst AI. Analyze the following meal/food description and return detailed nutritional information.

        Meal Type: %s
        Food Description: %s

        Return ONLY valid JSON with this exact structure (no markdown, no extra text):
        {
          "estimatedCalories": <integer>,
          "proteinGrams": <number>,
          "carbsGrams": <number>,
          "fatGrams": <number>,
          "fiberGrams": <number>,
          "confidenceScore": <integer 0-100>,
          "suggestions": "<brief suggestion for improvement>"
        }

        Rules:
        - Be as accurate as possible based on standard portion sizes
        - If the description is too vague, use reasonable defaults and lower confidence
        - If the input is not recognizable as food, return confidenceScore: 0 and estimatedCalories: 0
        """.formatted(mealType != null ? mealType : "meal", foodDescription);

        String response = aiService.askAI(prompt, true);
        return parseJsonResponse(response);
    }

    public Map<String, Object> suggestWorkout(String fitnessGoal, String activityLevel,
                                              String availableEquipment, String difficulty) {
        String prompt = """
        You are an expert personal trainer AI. Suggest a workout based on the following criteria.

        Fitness Goal: %s
        Activity Level: %s
        Available Equipment: %s
        Preferred Difficulty: %s

        Return ONLY valid JSON with this exact structure:
        {
          "suggestedWorkout": "<workout name>",
          "muscleGroup": "<primary muscle group>",
          "difficulty": "<beginner|intermediate|advanced>",
          "estimatedCaloriesBurn": <integer>,
          "durationMinutes": <integer>,
          "reasoning": "<brief explanation of why this workout suits the user>",
          "alternativeExercises": ["<exercise1>", "<exercise2>"],
          "nutritionTip": "<brief post-workout nutrition tip>"
        }
        """.formatted(
                fitnessGoal != null ? fitnessGoal : "general fitness",
                activityLevel != null ? activityLevel : "moderately active",
                availableEquipment != null ? availableEquipment : "none (bodyweight)",
                difficulty != null ? difficulty : "beginner"
        );

        String response = aiService.askAI(prompt, true);
        return parseJsonResponse(response);
    }

    public Map<String, Object> generateWorkoutPlan(String goal, String difficulty,
                                                   int durationWeeks, int sessionsPerWeek) {
        String prompt = """
        You are an expert fitness program designer AI. Create a detailed workout plan.

        Goal: %s
        Difficulty: %s
        Duration: %d weeks
        Sessions per week: %d

        Return ONLY valid JSON with this exact structure:
        {
          "name": "<plan name>",
          "description": "<brief description>",
          "difficulty": "<beginner|intermediate|advanced>",
          "durationWeeks": %d,
          "sessionsPerWeek": %d,
          "estimatedDailyCalories": <integer>,
          "exercises": [
            {
              "exerciseName": "<exercise name>",
              "description": "<brief description>",
              "sets": <integer>,
              "reps": <integer>,
              "durationSeconds": <integer or 0>,
              "restSeconds": <integer>,
              "muscleGroup": "<muscle group>",
              "equipment": "<equipment needed>",
              "orderIndex": <integer>
            }
          ]
        }
        """.formatted(goal, difficulty, durationWeeks, sessionsPerWeek, durationWeeks, sessionsPerWeek);

        String response = aiService.askAI(prompt, true);
        return parseJsonResponse(response);
    }

    public Map<String, Object> generateDailyMealPlan(Integer dailyCalorieTarget, String fitnessGoal,
                                                     String dietaryPreference) {
        String prompt = """
        You are a certified nutritionist AI. Create a daily meal plan.

        Daily Calorie Target: %d
        Fitness Goal: %s
        Dietary Preference: %s

        Return ONLY valid JSON with this exact structure:
        {
          "totalCalories": %d,
          "totalProtein": <number>,
          "totalCarbs": <number>,
          "totalFat": <number>,
          "meals": [
            {
              "mealType": "<BREAKFAST|LUNCH|DINNER|SNACK|PRE_WORKOUT|POST_WORKOUT>",
              "foodName": "<meal name>",
              "description": "<brief description>",
              "calories": <integer>,
              "proteinGrams": <number>,
              "carbsGrams": <number>,
              "fatGrams": <number>
            }
          ],
          "nutritionTips": "<brief nutrition advice>"
        }
        """.formatted(dailyCalorieTarget, fitnessGoal, dietaryPreference != null ? dietaryPreference : "none",
                dailyCalorieTarget);

        String response = aiService.askAI(prompt, true);
        return parseJsonResponse(response);
    }

    public Map<String, Object> suggestMealsForRemainingMacros(int remainingCalories,
                                                                double remainingProtein,
                                                                double remainingCarbs,
                                                                double remainingFat,
                                                                String mealType) {
        String prompt = """
        You are a practical nutritionist AI. A user needs to fill their remaining daily macros.
        Suggest 2-4 specific meal/snack ideas that would help them hit these remaining targets.

        Remaining Calories: %d
        Remaining Protein: %.1fg
        Remaining Carbs: %.1fg
        Remaining Fat: %.1fg
        Preferred meal time: %s

        Return ONLY valid JSON with this exact structure (no markdown, no extra text):
        {
          "suggestions": [
            {
              "suggestion": "<meal name>",
              "mealType": "<%s>",
              "estimatedCalories": <integer>,
              "estimatedProtein": <number>,
              "estimatedCarbs": <number>,
              "estimatedFat": <number>,
              "reasoning": "<why this fits the remaining macros>"
            }
          ]
        }

        Rules:
        - Each suggestion should realistically fit within remaining calories
        - Include a mix of protein, carb, and fat sources
        - Make suggestions practical and easy to prepare
        - If remaining is very small (< 100 cal), suggest light snacks
        """.formatted(remainingCalories, remainingProtein, remainingCarbs, remainingFat,
                mealType != null ? mealType : "any", mealType != null ? mealType : "SNACK");

        String response = aiService.askAI(prompt, true);
        return parseJsonResponse(response);
    }

    public String generateDailyNutritionTip(int totalCalories, double totalProtein, double totalCarbs, double totalFat,
                                              int goalCalories, int goalProtein, int goalCarbs, int goalFat,
                                              int mealCount, String fitnessGoal) {
        String prompt = """
        You are a friendly, practical nutrition coach AI. A user tracked their meals yesterday and needs a personalized tip for today.

        YESTERDAY'S SUMMARY:
        - Meals logged: %d
        - Calories: %d / %d goal (%+.0f%%)
        - Protein: %.1fg / %dg goal (%+.0f%%)
        - Carbs: %.1fg / %dg goal (%+.0f%%)
        - Fat: %.1fg / %dg goal (%+.0f%%)
        - Fitness Goal: %s

        Based on the above, provide ONE specific, actionable nutrition tip for today.
        The tip should:
        - Be concise (2-3 sentences max)
        - Reference their actual performance (e.g., great job hitting protein, or suggest ways to reduce carbs)
        - Be practical and easy to follow
        - Be encouraging and positive in tone
        - Reference their fitness goal: %s

        Return ONLY valid JSON with this exact structure (no markdown, no extra text):
        {
          "tip": "<your tip here>",
          "focusArea": "<calories|protein|carbs|fat|balance|hydration>",
          "suggestion": "<one specific food or habit suggestion>",
          "encouragement": "<one short encouraging sentence>"
        }
        """.formatted(
                mealCount,
                totalCalories, goalCalories,
                goalCalories > 0 ? ((double)(totalCalories - goalCalories) / goalCalories * 100) : 0,
                totalProtein, goalProtein,
                goalProtein > 0 ? ((totalProtein - goalProtein) / goalProtein * 100) : 0,
                totalCarbs, goalCarbs,
                goalCarbs > 0 ? ((totalCarbs - goalCarbs) / goalCarbs * 100) : 0,
                totalFat, goalFat,
                goalFat > 0 ? ((totalFat - goalFat) / goalFat * 100) : 0,
                fitnessGoal != null ? fitnessGoal : "general fitness",
                fitnessGoal != null ? fitnessGoal : "general fitness"
        );

        String response = aiService.askAI(prompt, true);
        return response;
    }

    public Map<String, Object> suggestGoalsForMetrics(String gender, double weightKg, double heightCm, int age,
                                                       double bmr, double tdeeModerate) {
        String prompt = """
        You are a fitness assessment AI. Based on a user's body metrics and calculated BMR, suggest the most appropriate fitness goals and activity levels.

        User Metrics:
        - Gender: %s
        - Weight: %.1f kg
        - Height: %.1f cm
        - Age: %d years

        Calculated BMR: %.0f kcal/day (Mifflin-St Jeor)
        Estimated TDEE at moderately active: %.0f kcal/day

        Return ONLY valid JSON with this exact structure (no markdown, no extra text):
        {
          "bmrEstimate": "<the BMR value as a readable string like '1,719 kcal/day'>",
          "tdeeEstimate": "<the TDEE value as a readable string>",
          "goalSuggestions": [
            {
              "value": "<LOSE_WEIGHT|BUILD_MUSCLE|INCREASE_STRENGTH|IMPROVE_ENDURANCE|GENERAL_FITNESS|SPORTS_PERFORMANCE|FLEXIBILITY>",
              "label": "<human-readable name like 'Lose Weight'>",
              "confidence": <integer 1-100>,
              "reasoning": "<2-3 sentence explanation of why this goal fits>"
            }
          ],
          "activitySuggestions": [
            {
              "value": "<SEDENTARY|LIGHTLY_ACTIVE|MODERATELY_ACTIVE|VERY_ACTIVE|EXTREMELY_ACTIVE>",
              "label": "<human-readable name like 'Moderately Active'>",
              "confidence": <integer 1-100>,
              "reasoning": "<1-2 sentence reasoning>"
            }
          ],
          "summary": "<1-2 sentence overall recommendation based on the metrics>"
        }

        Rules:
        - Return 3 goal suggestions ranked by confidence (highest first)
        - Return 2 activity level suggestions ranked by confidence
        - The BMR and TDEE values above are already calculated — use them directly in your response
        - Be encouraging and practical in the reasoning
        - Reference how different goals align with the user's metrics
        """.formatted(
                gender != null ? gender : "not specified",
                weightKg, heightCm, age,
                bmr, tdeeModerate
        );

        String response = aiService.askAI(prompt, true);
        return parseJsonResponse(response);
    }

    public String generateCoachNote(String prompt) {
        String fullPrompt = prompt + "\n\nReturn ONLY valid JSON.";
        return aiService.askAI(fullPrompt, true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String response) {
        if (response == null || response.startsWith("AI error:")) {
            return Map.of("error", response != null ? response : "AI service returned an empty response");
        }

        try {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (start == -1 || end <= start) {
                return Map.of("error", "AI did not return valid JSON. Raw: " + response.substring(0, Math.min(200, response.length())));
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.substring(start, end), Map.class);
        } catch (Exception e) {
            return Map.of("error", "Failed to parse AI response: " + e.getMessage());
        }
    }
}
