package com.example.service;

import com.example.dto.*;
import com.example.model.*;
import com.example.repository.UserRepository;
import com.example.repository.WorkoutPlanRepository;
import com.example.repository.WorkoutSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DashboardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

    private final UserService userService;
    private final NutritionService nutritionService;
    private final BodyMeasurementService bodyMeasurementService;
    private final WorkoutLoggingService workoutLoggingService;
    private final FitnessAIService fitnessAIService;
    private final UserRepository userRepository;
    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutPlanRepository planRepository;

    public DashboardService(UserService userService,
                            NutritionService nutritionService,
                            BodyMeasurementService bodyMeasurementService,
                            WorkoutLoggingService workoutLoggingService,
                            FitnessAIService fitnessAIService,
                            UserRepository userRepository,
                            WorkoutSessionRepository sessionRepository,
                            WorkoutPlanRepository planRepository) {
        this.userService = userService;
        this.nutritionService = nutritionService;
        this.bodyMeasurementService = bodyMeasurementService;
        this.workoutLoggingService = workoutLoggingService;
        this.fitnessAIService = fitnessAIService;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.planRepository = planRepository;
    }

    public DashboardDto getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardDto dto = new DashboardDto();

        // 1. Profile & goals
        dto.setProfile(buildProfile(user));

        // 2. Today's nutrition
        dto.setNutrition(buildNutrition(email));

        // 3. Latest body measurement
        dto.setMeasurement(buildMeasurement(email));

        // 4. Workout summary
        dto.setWorkout(buildWorkout(user));

        // 5. AI coach note (generated from the aggregated data)
        dto.setCoachNote(buildCoachNote(user, dto));

        return dto;
    }

    private DashboardDto.ProfileSection buildProfile(User user) {
        DashboardDto.ProfileSection s = new DashboardDto.ProfileSection();
        s.setUsername(user.getRealUsername());
        s.setEmail(user.getEmail());
        s.setFitnessGoal(user.getFitnessGoal() != null ? user.getFitnessGoal().name() : null);
        s.setActivityLevel(user.getActivityLevel() != null ? user.getActivityLevel().name() : null);
        s.setGender(user.getGender() != null ? user.getGender().name() : null);
        s.setWeightKg(user.getWeightKg());
        s.setHeightCm(user.getHeightCm());
        s.setDailyCalorieGoal(user.getDailyCalorieGoal());
        s.setDailyProteinGoal(user.getDailyProteinGoal());
        s.setDailyCarbsGoal(user.getDailyCarbsGoal());
        s.setDailyFatGoal(user.getDailyFatGoal());
        s.setOnboardingComplete(user.getGender() != null
                && user.getFitnessGoal() != null
                && user.getActivityLevel() != null
                && user.getWeightKg() != null
                && user.getHeightCm() != null
                && user.getDateOfBirth() != null);
        return s;
    }

    private DashboardDto.NutritionSection buildNutrition(String email) {
        try {
            DailyProgressDto progress = nutritionService.getDailyProgress(email, LocalDate.now());
            DashboardDto.NutritionSection s = new DashboardDto.NutritionSection();
            s.setDate(progress.getDate());
            s.setTotalCalories(progress.getTotalCalories());
            s.setTotalProtein(progress.getTotalProtein());
            s.setTotalCarbs(progress.getTotalCarbs());
            s.setTotalFat(progress.getTotalFat());
            s.setMealCount(progress.getMealCount());
            s.setCalorieGoal(progress.getCalorieGoal());
            s.setProteinGoal(progress.getProteinGoal());
            s.setCarbsGoal(progress.getCarbsGoal());
            s.setFatGoal(progress.getFatGoal());
            s.setCaloriePercent(progress.getCaloriePercent());
            s.setProteinPercent(progress.getProteinPercent());
            s.setCarbsPercent(progress.getCarbsPercent());
            s.setFatPercent(progress.getFatPercent());
            s.setCaloriesRemaining(progress.getCaloriesRemaining());
            s.setProteinRemaining(progress.getProteinRemaining());
            s.setCarbsRemaining(progress.getCarbsRemaining());
            s.setFatRemaining(progress.getFatRemaining());
            return s;
        } catch (Exception e) {
            LOGGER.warn("Could not build nutrition section for dashboard", e);
            DashboardDto.NutritionSection s = new DashboardDto.NutritionSection();
            s.setDate(LocalDate.now());
            return s;
        }
    }

    private DashboardDto.MeasurementSection buildMeasurement(String email) {
        DashboardDto.MeasurementSection s = new DashboardDto.MeasurementSection();
        try {
            Map<String, Object> summary = bodyMeasurementService.getSummary(email);
            if (Boolean.TRUE.equals(summary.get("hasData"))) {
                s.setHasData(true);
                s.setLatestDate(summary.get("latestDate") != null
                        ? LocalDate.parse(summary.get("latestDate").toString()) : null);
                s.setWeightKg(summary.get("currentWeightKg") instanceof Number n ? n.doubleValue() : null);
                s.setBodyFatPercent(summary.get("currentBodyFatPercent") instanceof Number n ? n.doubleValue() : null);
                s.setWaistCm(summary.get("currentWaistCm") instanceof Number n ? n.doubleValue() : null);
                s.setWeightChangeKg(summary.get("weightChangeKg") instanceof Number n ? n.doubleValue() : null);
                s.setBodyFatChange(summary.get("bodyFatChange") instanceof Number n ? n.doubleValue() : null);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not build measurement section for dashboard", e);
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private DashboardDto.WorkoutSection buildWorkout(User user) {
        DashboardDto.WorkoutSection s = new DashboardDto.WorkoutSection();
        try {
            Map<String, Object> summary = workoutLoggingService.getWorkoutSummary(user.getEmail());
            s.setTotalSessions(((Number) summary.getOrDefault("totalSessions", 0)).longValue());
            s.setSessionsThisWeek(((Number) summary.getOrDefault("sessionsThisWeek", 0)).longValue());
            s.setTotalMinutesThisWeek(((Number) summary.getOrDefault("totalMinutesThisWeek", 0)).intValue());
            s.setTotalCaloriesBurnedThisWeek(((Number) summary.getOrDefault("totalCaloriesBurnedThisWeek", 0)).intValue());

            // Find current plan (most recent plan with active sessions)
            List<WorkoutSession> recentSessions = sessionRepository
                    .findByMemberIdAndCompletedTrueOrderByStartedAtDesc(user.getId());
            if (!recentSessions.isEmpty()) {
                WorkoutSession latest = recentSessions.get(0);
                if (latest.getWorkoutPlan() != null) {
                    s.setCurrentPlanName(latest.getWorkoutPlan().getName());
                }
            }

            // Last 3 recent sessions
            s.setRecentSessions(recentSessions.stream()
                    .limit(3)
                    .map(this::toSessionSummary)
                    .toList());

        } catch (Exception e) {
            LOGGER.warn("Could not build workout section for dashboard", e);
        }
        return s;
    }

    private WorkoutSessionDto toSessionSummary(WorkoutSession session) {
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setId(session.getId());
        dto.setSessionName(session.getSessionName());
        dto.setStartedAt(session.getStartedAt());
        dto.setCompletedAt(session.getCompletedAt());
        dto.setCompleted(session.getCompleted());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setCaloriesBurned(session.getCaloriesBurned());
        dto.setIntensity(session.getIntensity());
        dto.setRating(session.getRating());
        if (session.getWorkoutPlan() != null) {
            dto.setPlanId(session.getWorkoutPlan().getId());
            dto.setPlanName(session.getWorkoutPlan().getName());
        }
        return dto;
    }

    private DashboardDto.CoachNote buildCoachNote(User user, DashboardDto dashboard) {
        // Build a prompt from the aggregated dashboard data
        String goal = user.getFitnessGoal() != null ? user.getFitnessGoal().name().replace("_", " ").toLowerCase() : "general fitness";
        String level = user.getActivityLevel() != null ? user.getActivityLevel().name().replace("_", " ").toLowerCase() : "moderately active";

        DashboardDto.NutritionSection n = dashboard.getNutrition();
        DashboardDto.WorkoutSection w = dashboard.getWorkout();
        DashboardDto.MeasurementSection m = dashboard.getMeasurement();

        StringBuilder context = new StringBuilder();
        context.append("User: ").append(user.getRealUsername()).append("\n");
        context.append("Goal: ").append(goal).append("\n");
        context.append("Activity Level: ").append(level).append("\n");

        // Nutrition context
        if (n != null) {
            context.append("Today's Nutrition: ").append(n.getMealCount()).append(" meals, ")
                   .append(n.getTotalCalories()).append("/").append(n.getCalorieGoal() != null ? n.getCalorieGoal() : "?").append(" kcal, ")
                   .append("protein ").append(String.format("%.1f", n.getTotalProtein())).append("/").append(n.getProteinGoal() != null ? n.getProteinGoal() : "?").append("g\n");
        }

        // Workout context
        if (w != null) {
            context.append("Workouts This Week: ").append(w.getSessionsThisWeek()).append(" sessions, ")
                   .append(w.getTotalMinutesThisWeek()).append(" min, ")
                   .append(w.getTotalCaloriesBurnedThisWeek()).append(" kcal burned\n");
            if (w.getCurrentPlanName() != null) {
                context.append("Current Plan: ").append(w.getCurrentPlanName()).append("\n");
            }
        }

        // Measurement context
        if (m != null && m.isHasData()) {
            context.append("Latest Weight: ").append(m.getWeightKg()).append(" kg");
            if (m.getWeightChangeKg() != null) {
                context.append(" (").append(m.getWeightChangeKg() > 0 ? "+" : "").append(String.format("%.1f", m.getWeightChangeKg())).append(" kg change)");
            }
            context.append("\n");
            if (m.getBodyFatPercent() != null) {
                context.append("Body Fat: ").append(m.getBodyFatPercent()).append("%");
                if (m.getBodyFatChange() != null) {
                    context.append(" (").append(m.getBodyFatChange() > 0 ? "+" : "").append(String.format("%.1f", m.getBodyFatChange())).append(" change)");
                }
                context.append("\n");
            }
        }

        String prompt = """
        You are a supportive, knowledgeable fitness coach. Review this member's current dashboard data and write a brief, personalized coach note.

        %s

        Based on the above, provide ONE encouraging but honest coach note.
        Focus on what's going well and one specific area to improve.
        Keep it to 2-3 sentences. Be warm and practical.

        Return ONLY valid JSON with this exact structure (no markdown):
        {
          "message": "<the coach note, 2-3 sentences>",
          "focus": "<the main focus area: nutrition|workout|consistency|measurements|rest|general>",
          "suggestion": "<one specific action they can take today>"
        }
        """.formatted(context.toString());

        try {
            String response = fitnessAIService.generateCoachNote(prompt);
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            if (start >= 0 && end > start) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> parsed = mapper.readValue(response.substring(start, end), Map.class);
                DashboardDto.CoachNote note = new DashboardDto.CoachNote();
                note.setMessage(parsed.getOrDefault("message", "Keep up the great work!").toString());
                note.setFocus(parsed.getOrDefault("focus", "general").toString());
                note.setSuggestion(parsed.getOrDefault("suggestion", "Stay consistent with your current routine.").toString());
                return note;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to generate AI coach note, using fallback", e);
        }

        // Fallback coach note
        return new DashboardDto.CoachNote(
                "You're making progress! Keep tracking your meals and sticking to your workout routine. Every day counts.",
                "general",
                "Log your next meal and complete today's workout."
        );
    }
}
