package com.example.service;

import com.example.model.MealEntry;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.MealEntryRepository;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Scheduled service that runs every morning at 7 AM.
 * For each member who has daily tips enabled, it:
 * 1. Fetches yesterday's meal entries
 * 2. Compares actual macros vs goals
 * 3. Generates a personalized AI tip
 * 4. Sends it via email
 */
@Service
public class DailyNutritionTipService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyNutritionTipService.class);

    private final UserRepository userRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FitnessAIService fitnessAIService;
    private final EmailService emailService;

    public DailyNutritionTipService(UserRepository userRepository,
                                    MealEntryRepository mealEntryRepository,
                                    FitnessAIService fitnessAIService,
                                    EmailService emailService) {
        this.userRepository = userRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.fitnessAIService = fitnessAIService;
        this.emailService = emailService;
    }

    /**
     * Runs every day at 7:00 AM. Processes all members who have opted into daily tips.
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void sendDailyNutritionTips() {
        LOGGER.info("Starting daily nutrition tip dispatch...");

        List<User> optedInMembers = userRepository.findByRoleAndDailyTipEnabledTrue(Role.ROLE_MEMBER);

        if (optedInMembers.isEmpty()) {
            LOGGER.info("No members have daily tips enabled. Skipping.");
            return;
        }

        LOGGER.info("Found {} members with daily tips enabled", optedInMembers.size());

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(LocalTime.MAX);

        for (User member : optedInMembers) {
            try {
                processMember(member, yesterday, start, end);
            } catch (Exception e) {
                LOGGER.error("Failed to send daily tip to member {} ({})", member.getEmail(), member.getId(), e);
            }
        }

        LOGGER.info("Daily nutrition tip dispatch complete.");
    }

    private void processMember(User member, LocalDate yesterday,
                                LocalDateTime start, LocalDateTime end) {
        // Skip if member has no goals set
        if (member.getDailyCalorieGoal() == null || member.getDailyCalorieGoal() <= 0) {
            LOGGER.debug("Skipping member {} — no daily calorie goal set", member.getEmail());
            return;
        }

        // Skip if no email
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            LOGGER.debug("Skipping member {} — no email", member.getId());
            return;
        }

        // Fetch yesterday's meals
        List<MealEntry> meals = mealEntryRepository
                .findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(member.getId(), start, end);

        int totalCalories = meals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
        double totalProtein = meals.stream().mapToDouble(m -> m.getProteinGrams() != null ? m.getProteinGrams() : 0).sum();
        double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbsGrams() != null ? m.getCarbsGrams() : 0).sum();
        double totalFat = meals.stream().mapToDouble(m -> m.getFatGrams() != null ? m.getFatGrams() : 0).sum();
        int mealCount = meals.size();

        Integer goalCalories = member.getDailyCalorieGoal();
        Integer goalProtein = member.getDailyProteinGoal();
        Integer goalCarbs = member.getDailyCarbsGoal();
        Integer goalFat = member.getDailyFatGoal();

        String fitnessGoal = member.getFitnessGoal() != null ? member.getFitnessGoal().name() : "GENERAL_FITNESS";

        // Generate AI tip
        String rawResponse = fitnessAIService.generateDailyNutritionTip(
                totalCalories, totalProtein, totalCarbs, totalFat,
                goalCalories != null ? goalCalories : 0,
                goalProtein != null ? goalProtein : 0,
                goalCarbs != null ? goalCarbs : 0,
                goalFat != null ? goalFat : 0,
                mealCount,
                fitnessGoal
        );

        // Parse the AI response
        String tip = "Keep tracking your meals and stay consistent!";
        String focusArea = "balance";
        String suggestion = "Try adding more variety to your meals today.";
        String encouragement = "You're doing great — every day is a step forward!";

        try {
            int startIdx = rawResponse.indexOf("{");
            int endIdx = rawResponse.lastIndexOf("}") + 1;
            if (startIdx >= 0 && endIdx > startIdx) {
                String json = rawResponse.substring(startIdx, endIdx);
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(json, Map.class);

                tip = parsed.getOrDefault("tip", tip).toString();
                focusArea = parsed.getOrDefault("focusArea", focusArea).toString();
                suggestion = parsed.getOrDefault("suggestion", suggestion).toString();
                encouragement = parsed.getOrDefault("encouragement", encouragement).toString();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to parse AI tip for member {}, using fallback", member.getEmail(), e);
        }

        String username = member.getRealUsername() != null ? member.getRealUsername() : "there";

        // Send email
        emailService.sendDailyNutritionTipEmail(
                member.getEmail(), username, tip, focusArea, suggestion, encouragement,
                totalCalories, goalCalories != null ? goalCalories : 0,
                totalProtein, goalProtein != null ? goalProtein : 0,
                totalCarbs, goalCarbs != null ? goalCarbs : 0,
                totalFat, goalFat != null ? goalFat : 0
        );

        LOGGER.info("Sent daily nutrition tip to {} (focus: {})", member.getEmail(), focusArea);
    }
}
