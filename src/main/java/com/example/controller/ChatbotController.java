package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.ChatbotService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final UserRepository userRepository;

    public ChatbotController(ChatbotService chatbotService,
                             UserRepository userRepository) {
        this.chatbotService = chatbotService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Map<String, String> chat(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String message = body.get("message");

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("member");

        // Build goal context for the AI
        String goalContext = userRepository.findByEmail(authentication.getName())
                .map(u -> buildGoalContext(u))
                .orElse("");

        String reply = chatbotService.chat(role, message, goalContext);
        return Map.of("reply", reply);
    }

    private String buildGoalContext(User user) {
        StringBuilder ctx = new StringBuilder();
        if (user.getGender() != null) {
            ctx.append("The user's gender is: ")
               .append(user.getGender().name().toLowerCase())
               .append(". ");
        }
        if (user.getFitnessGoal() != null) {
            ctx.append("The user's fitness goal is: ")
               .append(user.getFitnessGoal().name().replace("_", " ").toLowerCase())
               .append(". ");
        }
        if (user.getActivityLevel() != null) {
            ctx.append("Their activity level is: ")
               .append(user.getActivityLevel().name().replace("_", " ").toLowerCase())
               .append(". ");
        }
        if (user.getDailyCalorieGoal() != null) {
            ctx.append("Their daily calorie goal is: ")
               .append(user.getDailyCalorieGoal())
               .append(" kcal. ");
        }
        if (user.getWeightKg() != null) {
            ctx.append("Their weight is: ")
               .append(user.getWeightKg())
               .append(" kg. ");
        }
        return ctx.toString();
    }
}
