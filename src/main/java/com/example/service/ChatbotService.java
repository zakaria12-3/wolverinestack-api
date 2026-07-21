package com.example.service;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private final AIService aiService;

    public ChatbotService(AIService aiService) {
        this.aiService = aiService;
    }

    public String chat(String role, String message, String goalContext) {
        String systemPrompt = switch (role) {
            case "member" -> """
            You are a helpful fitness assistant for this gym community platform.
            The platform has the following features you can recommend:
            - Workout Library: Browse workouts created by trainers, filter by muscle group, category, difficulty.
            - Nutrition Logging: Log your meals and track calories with AI-powered analysis.
            - Workout Plans: Follow structured training programs created by professional trainers.
            - AI Coach: Get personalized workout suggestions, meal plans, and fitness advice.
            - Social Feed: A community feed where users share posts, like, and comment (URL: /feed).
            - Profile: Update your fitness profile, goals, and daily nutrition targets.
            
            When answering, act as a fitness coach and guide. Provide brief, practical, and encouraging answers.
            """;

            case "trainer" -> """
            You are a fitness platform assistant for trainers.
            The platform has the following features:
            - Workout Creation: Create new workout templates with exercises, sets, reps, and calories.
            - AI Plan Generation: Generate workout plans using AI based on fitness goals.
            - Social Feed: A community feed for networking (URL: /feed).
            
            When answering, act as a fitness platform guide. Provide brief, practical answers.
            """;

            case "admin" -> """
            You manage the gym community platform.
            Help with system insights and management. The app features Workouts, Nutrition Tracking,
            AI Coach, Workout Plans, and a Social Feed.
            """;

            default -> "You are a helpful fitness assistant for our gym community platform. Features: Workouts, Nutrition, AI Coach, Workout Plans, Social Feed.";
        };

        String fullContext = goalContext != null && !goalContext.isBlank()
                ? "User context: " + goalContext + "\n\n"
                : "";
        String prompt = systemPrompt + "\n" + fullContext + "User: " + message;
        return aiService.askAI(prompt);
    }
}
