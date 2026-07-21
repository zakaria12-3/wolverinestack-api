package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.AIService;
import com.example.service.FitnessAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class FitnessAIController {

    private final AIService aiService;
    private final FitnessAIService fitnessAIService;
    private final UserRepository userRepository;

    public FitnessAIController(AIService aiService, FitnessAIService fitnessAIService,
                               UserRepository userRepository) {
        this.aiService = aiService;
        this.fitnessAIService = fitnessAIService;
        this.userRepository = userRepository;
    }

    /**
     * Resolve the user's fitness goal from their profile, falling back to what's in the request body.
     */
    private String resolveFitnessGoal(Authentication auth, String bodyGoal) {
        if (bodyGoal != null && !bodyGoal.isBlank() && !bodyGoal.equalsIgnoreCase("auto")) {
            return bodyGoal;
        }
        return userRepository.findByEmail(auth.getName())
                .map(u -> u.getFitnessGoal() != null ? u.getFitnessGoal().name() : null)
                .orElse(null);
    }

    private String resolveActivityLevel(Authentication auth, String bodyLevel) {
        if (bodyLevel != null && !bodyLevel.isBlank() && !bodyLevel.equalsIgnoreCase("auto")) {
            return bodyLevel;
        }
        return userRepository.findByEmail(auth.getName())
                .map(u -> u.getActivityLevel() != null ? u.getActivityLevel().name() : null)
                .orElse(null);
    }

    @PostMapping("/analyze-meal")
    public ResponseEntity<?> analyzeMeal(@RequestBody Map<String, String> body,
                                          Authentication authentication) {
        String foodDescription = body.get("foodDescription");
        String mealType = body.get("mealType");
        try {
            return ResponseEntity.ok(fitnessAIService.analyzeMeal(foodDescription, mealType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/suggest-workout")
    public ResponseEntity<?> suggestWorkout(@RequestBody Map<String, String> body,
                                             Authentication authentication) {
        // Auto-inject user's fitness goal and activity level from profile if not explicitly provided
        String fitnessGoal = resolveFitnessGoal(authentication, body.get("fitnessGoal"));
        String activityLevel = resolveActivityLevel(authentication, body.get("activityLevel"));
        String equipment = body.get("equipment");
        String difficulty = body.get("difficulty");
        try {
            return ResponseEntity.ok(fitnessAIService.suggestWorkout(
                    fitnessGoal, activityLevel, equipment, difficulty));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate-plan")
    public ResponseEntity<?> generatePlan(@RequestBody Map<String, Object> body,
                                           Authentication authentication) {
        try {
            String bodyGoal = (String) body.getOrDefault("goal", "auto");
            String goal = resolveFitnessGoal(authentication, bodyGoal);
            if (goal == null) goal = "GENERAL_FITNESS";
            
            String difficulty = (String) body.getOrDefault("difficulty", "beginner");
            int weeks = body.get("durationWeeks") instanceof Number n ? n.intValue() : 4;
            int sessions = body.get("sessionsPerWeek") instanceof Number n ? n.intValue() : 3;
            return ResponseEntity.ok(fitnessAIService.generateWorkoutPlan(goal, difficulty, weeks, sessions));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/meal-plan")
    public ResponseEntity<?> generateMealPlan(@RequestBody Map<String, Object> body,
                                               Authentication authentication) {
        try {
            int calories = body.get("dailyCalorieTarget") instanceof Number n ? n.intValue() : 2000;
            String bodyGoal = (String) body.getOrDefault("fitnessGoal", "auto");
            String goal = resolveFitnessGoal(authentication, bodyGoal);
            if (goal == null) goal = "GENERAL_FITNESS";
            
            String preference = (String) body.getOrDefault("dietaryPreference", "");
            return ResponseEntity.ok(fitnessAIService.generateDailyMealPlan(calories, goal, preference));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> body,
                        Authentication authentication) {
        String message = body.get("message");
        // Include user's fitness goal in the AI context
        String goalContext = userRepository.findByEmail(authentication.getName())
                .map(u -> {
                    String goal = u.getFitnessGoal() != null ? u.getFitnessGoal().name().replace("_", " ").toLowerCase() : "general fitness";
                    String level = u.getActivityLevel() != null ? u.getActivityLevel().name().replace("_", " ").toLowerCase() : "moderately active";
                    return "The user's fitness goal is: " + goal + ". Their activity level is: " + level + ". ";
                })
                .orElse("");

        String prompt = "You are an expert fitness and nutrition AI assistant. Help the user with their fitness, workout, and nutrition questions. Be concise and practical. "
                + goalContext
                + "\n\nUser: " + message;
        return aiService.askAI(prompt);
    }
}
