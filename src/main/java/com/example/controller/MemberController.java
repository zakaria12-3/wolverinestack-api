package com.example.controller;

import com.example.dto.*;
import com.example.model.User;
import com.example.service.BodyMeasurementService;
import com.example.service.DashboardService;
import com.example.service.MealPlannerService;
import com.example.service.NutritionService;
import com.example.service.UserService;
import com.example.service.WorkoutLoggingService;
import com.example.service.WorkoutPlanService;
import com.example.service.WorkoutService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final WorkoutService workoutService;
    private final NutritionService nutritionService;
    private final WorkoutPlanService workoutPlanService;
    private final WorkoutLoggingService workoutLoggingService;
    private final MealPlannerService mealPlannerService;
    private final BodyMeasurementService bodyMeasurementService;
    private final UserService userService;
    private final DashboardService dashboardService;

    public MemberController(WorkoutService workoutService,
                            NutritionService nutritionService,
                            WorkoutPlanService workoutPlanService,
                            WorkoutLoggingService workoutLoggingService,
                            MealPlannerService mealPlannerService,
                            BodyMeasurementService bodyMeasurementService,
                            UserService userService,
                            DashboardService dashboardService) {
        this.workoutService = workoutService;
        this.nutritionService = nutritionService;
        this.workoutPlanService = workoutPlanService;
        this.workoutLoggingService = workoutLoggingService;
        this.mealPlannerService = mealPlannerService;
        this.bodyMeasurementService = bodyMeasurementService;
        this.userService = userService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> dashboard(Authentication authentication) {
        return ResponseEntity.ok(
                dashboardService.getDashboard(authentication.getName())
        );
    }

    // Workout browsing
    @GetMapping("/workouts")
    public List<com.example.dto.WorkoutDto> getWorkouts(Authentication authentication) {
        return workoutService.getAllWorkouts().stream()
                .map(w -> {
                    WorkoutDto dto = new WorkoutDto();
                    dto.setId(w.getId());
                    dto.setName(w.getName());
                    dto.setDescription(w.getDescription());
                    dto.setInstructions(w.getInstructions());
                    dto.setMuscleGroup(w.getMuscleGroup());
                    dto.setCategory(w.getCategory());
                    dto.setDifficulty(w.getDifficulty());
                    dto.setEquipment(w.getEquipment());
                    dto.setEstimatedCaloriesBurned(w.getEstimatedCaloriesBurned());
                    dto.setEstimatedDurationMinutes(w.getEstimatedDurationMinutes());
                    dto.setSets(w.getSets());
                    dto.setReps(w.getReps());
                    dto.setImageUrl(w.getImageUrl());
                    dto.setVideoUrl(w.getVideoUrl());
                    dto.setCreatedAt(w.getCreatedAt());
                    if (w.getTrainer() != null) {
                        dto.setTrainerName(w.getTrainer().getRealUsername());
                    }
                    return dto;
                })
                .toList();
    }

    // ========== Nutrition / Meal Logging ==========
    @PostMapping("/meals")
    public ResponseEntity<?> logMeal(@RequestBody LogMealRequestDto request,
                                     Authentication authentication) throws Exception {
        return ResponseEntity.ok(nutritionService.logMeal(request, authentication.getName()));
    }

    @GetMapping("/meals")
    public ResponseEntity<List<MealEntryDto>> getMeals(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.getMealsForMember(authentication.getName()));
    }

    @GetMapping("/meals/today")
    public ResponseEntity<Map<String, Object>> getTodayMeals(Authentication authentication) {
        return ResponseEntity.ok(
                nutritionService.getDailyNutritionSummary(authentication.getName(), LocalDate.now())
        );
    }

    @GetMapping("/meals/date/{date}")
    public ResponseEntity<Map<String, Object>> getMealsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        return ResponseEntity.ok(
                nutritionService.getDailyNutritionSummary(authentication.getName(), date)
        );
    }

    @PutMapping("/meals/{id}")
    public ResponseEntity<MealEntryDto> updateMeal(@PathVariable Long id,
                                                   @RequestBody LogMealRequestDto request,
                                                   Authentication authentication) {
        return ResponseEntity.ok(
                nutritionService.updateMealEntry(id, request, authentication.getName())
        );
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<?> deleteMeal(@PathVariable Long id, Authentication authentication) {
        nutritionService.deleteMealEntry(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    // ========== TDEE & Goal Tracking ==========

    /** Calculate TDEE based on user's profile */
    @GetMapping("/nutrition/tdee")
    public ResponseEntity<TdeeResultDto> calculateTdee(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.calculateTdee(authentication.getName()));
    }

    /** Calculate TDEE and auto-set daily goals */
    @PostMapping("/nutrition/tdee/apply")
    public ResponseEntity<TdeeResultDto> applyTdee(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.applyTdeeToGoals(authentication.getName()));
    }

    /** Daily progress with goal percentages for progress bars */
    @GetMapping("/nutrition/progress")
    public ResponseEntity<DailyProgressDto> getDailyProgress(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(nutritionService.getDailyProgress(authentication.getName(), targetDate));
    }

    /** Weekly nutrition report with averages and macro split */
    @GetMapping("/nutrition/weekly-report")
    public ResponseEntity<WeeklyReportDto> getWeeklyReport(Authentication authentication) {
        return ResponseEntity.ok(nutritionService.getWeeklyReport(authentication.getName()));
    }

    // ========== Body Measurement Tracking ==========

    /** Log a new body measurement */
    @PostMapping("/measurements")
    public ResponseEntity<BodyMeasurementDto> logMeasurement(
            @RequestBody BodyMeasurementRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                bodyMeasurementService.logMeasurement(authentication.getName(), request)
        );
    }

    /** Get all body measurements */
    @GetMapping("/measurements")
    public ResponseEntity<List<BodyMeasurementDto>> getMeasurements(Authentication authentication) {
        return ResponseEntity.ok(bodyMeasurementService.getMeasurements(authentication.getName()));
    }

    /** Get measurements for a date range */
    @GetMapping("/measurements/range")
    public ResponseEntity<List<BodyMeasurementDto>> getMeasurementsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication) {
        return ResponseEntity.ok(
                bodyMeasurementService.getMeasurementsForRange(authentication.getName(), start, end)
        );
    }

    /** Get the latest measurement */
    @GetMapping("/measurements/latest")
    public ResponseEntity<BodyMeasurementDto> getLatestMeasurement(Authentication authentication) {
        return ResponseEntity.ok(bodyMeasurementService.getLatestMeasurement(authentication.getName()));
    }

    /** Get progress data with optional nutrition overlay for charts */
    @GetMapping("/measurements/progress")
    public ResponseEntity<BodyMeasurementProgressDto> getMeasurementProgress(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication) {
        return ResponseEntity.ok(
                bodyMeasurementService.getProgress(authentication.getName(), start, end)
        );
    }

    /** Update a measurement */
    @PutMapping("/measurements/{id}")
    public ResponseEntity<BodyMeasurementDto> updateMeasurement(
            @PathVariable Long id,
            @RequestBody BodyMeasurementRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                bodyMeasurementService.updateMeasurement(id, authentication.getName(), request)
        );
    }

    /** Delete a measurement */
    @DeleteMapping("/measurements/{id}")
    public ResponseEntity<?> deleteMeasurement(
            @PathVariable Long id,
            Authentication authentication) {
        bodyMeasurementService.deleteMeasurement(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    /** Get measurement summary (latest + changes) */
    @GetMapping("/measurements/summary")
    public ResponseEntity<Map<String, Object>> getMeasurementSummary(Authentication authentication) {
        return ResponseEntity.ok(bodyMeasurementService.getSummary(authentication.getName()));
    }

    // ========== Meal Planner ==========

    /** Add a meal to the plan for a specific date */
    @PostMapping("/meal-plan")
    public ResponseEntity<PlannedMealDto> addMealToPlan(
            @RequestBody MealPlannerRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                mealPlannerService.addMealToPlan(authentication.getName(), request)
        );
    }

    /** Get the full meal plan with projection vs goals and AI suggestions */
    @GetMapping("/meal-plan/projection")
    public ResponseEntity<MealPlanProjectionDto> getProjection(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(
                mealPlannerService.getProjection(authentication.getName(), targetDate)
        );
    }

    /** Get all planned meals for a date */
    @GetMapping("/meal-plan")
    public ResponseEntity<List<PlannedMealDto>> getMealPlan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(
                mealPlannerService.getMealPlan(authentication.getName(), targetDate)
        );
    }

    /** Update a planned meal */
    @PutMapping("/meal-plan/{id}")
    public ResponseEntity<PlannedMealDto> updatePlannedMeal(
            @PathVariable Long id,
            @RequestBody MealPlannerRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                mealPlannerService.updatePlannedMeal(id, authentication.getName(), request)
        );
    }

    /** Delete a planned meal */
    @DeleteMapping("/meal-plan/{id}")
    public ResponseEntity<?> deletePlannedMeal(
            @PathVariable Long id,
            Authentication authentication) {
        mealPlannerService.deletePlannedMeal(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    /** Mark a planned meal as consumed (logs it as actual meal) */
    @PostMapping("/meal-plan/{id}/consume")
    public ResponseEntity<?> markAsConsumed(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(
                mealPlannerService.markAsConsumed(id, authentication.getName())
        );
    }

    // ========== Workout Plan CRUD ==========

    /** Get all plans available to the member (their own + trainer-created) */
    @GetMapping("/plans")
    public List<WorkoutPlanDto> getPlans(Authentication authentication) {
        return workoutPlanService.getPlansForMember(authentication.getName());
    }

    @GetMapping("/plans/{planId}")
    public WorkoutPlanDto getPlan(@PathVariable Long planId) {
        return workoutPlanService.getPlanDtoById(planId);
    }

    /** Create a new workout plan (owned by this member) */
    @PostMapping("/plans")
    public WorkoutPlanDto createPlan(@RequestBody CreateWorkoutPlanDto dto,
                                     Authentication authentication) {
        return workoutPlanService.createPlanForMember(authentication.getName(), dto);
    }

    /** Update a workout plan (must be owned by this member) */
    @PutMapping("/plans/{planId}")
    public WorkoutPlanDto updatePlan(@PathVariable Long planId,
                                     @RequestBody CreateWorkoutPlanDto dto,
                                     Authentication authentication) {
        return workoutPlanService.updatePlanForMember(planId, authentication.getName(), dto);
    }

    /** Delete a workout plan (must be owned by this member) */
    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId,
                                        Authentication authentication) {
        workoutPlanService.deletePlanForMember(planId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    /** Generate an AI-powered workout plan draft */
    @PostMapping("/plans/generate")
    public ResponseEntity<?> generatePlan(@RequestBody Map<String, Object> body) {
        try {
            String goal = (String) body.getOrDefault("goal", "general_fitness");
            String difficulty = (String) body.getOrDefault("difficulty", "beginner");
            int weeks = body.get("durationWeeks") instanceof Number n ? n.intValue() : 4;
            int sessions = body.get("sessionsPerWeek") instanceof Number n ? n.intValue() : 3;
            return ResponseEntity.ok(
                    workoutPlanService.generatePlanDraft(goal, difficulty, weeks, sessions)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "hint", "If the error mentions 'no JSON found', the AI model returned plain text. Try again."
            ));
        }
    }

    // ========== Workout Session Logging ==========

    /** Start a new workout session */
    @PostMapping("/sessions/start")
    public ResponseEntity<WorkoutSessionDto> startSession(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        String sessionName = (String) body.getOrDefault("sessionName", null);
        Long planId = body.get("planId") instanceof Number n ? n.longValue() : null;
        return ResponseEntity.ok(
                workoutLoggingService.startSession(authentication.getName(), sessionName, planId)
        );
    }

    /** Add an exercise to an active session (shell — sets are added separately) */
    @PostMapping("/sessions/{sessionId}/exercises")
    public ResponseEntity<WorkoutSessionDto> addExercise(
            @PathVariable Long sessionId,
            @RequestBody LogExerciseRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.addExerciseToSession(sessionId, authentication.getName(), request)
        );
    }

    /** Remove an exercise from an active session */
    @DeleteMapping("/sessions/{sessionId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutSessionDto> removeExercise(
            @PathVariable Long sessionId,
            @PathVariable Long exerciseId,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.removeExerciseFromSession(sessionId, exerciseId, authentication.getName())
        );
    }

    /** Log a single set to a session exercise (Hevy-style) */
    @PostMapping("/sessions/{sessionId}/exercises/{exerciseId}/sets")
    public ResponseEntity<SessionExerciseDto> logSet(
            @PathVariable Long sessionId,
            @PathVariable Long exerciseId,
            @RequestBody LogSetRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.logSet(sessionId, exerciseId, authentication.getName(), request)
        );
    }

    /** Update an existing set */
    @PutMapping("/sessions/{sessionId}/exercises/{exerciseId}/sets/{setId}")
    public ResponseEntity<SessionExerciseDto> updateSet(
            @PathVariable Long sessionId,
            @PathVariable Long exerciseId,
            @PathVariable Long setId,
            @RequestBody LogSetRequestDto request,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.updateSet(sessionId, exerciseId, setId, authentication.getName(), request)
        );
    }

    /** Delete a single set */
    @DeleteMapping("/sessions/{sessionId}/exercises/{exerciseId}/sets/{setId}")
    public ResponseEntity<SessionExerciseDto> deleteSet(
            @PathVariable Long sessionId,
            @PathVariable Long exerciseId,
            @PathVariable Long setId,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.deleteSet(sessionId, exerciseId, setId, authentication.getName())
        );
    }

    /** Bulk-log multiple sets at once (for fast entry) */
    @PostMapping("/sessions/{sessionId}/exercises/{exerciseId}/sets/bulk")
    public ResponseEntity<SessionExerciseDto> bulkLogSets(
            @PathVariable Long sessionId,
            @PathVariable Long exerciseId,
            @RequestBody List<LogSetRequestDto> requests,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.bulkLogSets(sessionId, exerciseId, authentication.getName(), requests)
        );
    }

    /** Complete a workout session */
    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<WorkoutSessionDto> completeSession(
            @PathVariable Long sessionId,
            @RequestBody(required = false) Map<String, Object> completionData,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.completeSession(sessionId, authentication.getName(), completionData)
        );
    }

    /** Get all my sessions */
    @GetMapping("/sessions")
    public ResponseEntity<List<WorkoutSessionDto>> getMySessions(Authentication authentication) {
        return ResponseEntity.ok(workoutLoggingService.getMySessions(authentication.getName()));
    }

    /** Get a single session */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<WorkoutSessionDto> getSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        return ResponseEntity.ok(workoutLoggingService.getSession(sessionId, authentication.getName()));
    }

    /** Get sessions for a date range */
    @GetMapping("/sessions/range")
    public ResponseEntity<List<WorkoutSessionDto>> getSessionsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.getSessionsForDateRange(authentication.getName(), start, end)
        );
    }

    /** Delete a session */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        workoutLoggingService.deleteSession(sessionId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    // ========== Progress Tracking ==========

    /** Get progress data for a specific exercise */
    @GetMapping("/progress/{exerciseName}")
    public ResponseEntity<ProgressDataDto> getProgress(
            @PathVariable String exerciseName,
            Authentication authentication) {
        return ResponseEntity.ok(
                workoutLoggingService.getProgress(authentication.getName(), exerciseName)
        );
    }

    /** Get all tracked exercises for the member */
    @GetMapping("/progress/exercises")
    public ResponseEntity<List<String>> getTrackedExercises(Authentication authentication) {
        return ResponseEntity.ok(workoutLoggingService.getTrackedExercises(authentication.getName()));
    }

    /** Get workout summary stats */
    @GetMapping("/progress/summary")
    public ResponseEntity<Map<String, Object>> getWorkoutSummary(Authentication authentication) {
        return ResponseEntity.ok(workoutLoggingService.getWorkoutSummary(authentication.getName()));
    }

    /** Get all-time best PRs for every exercise */
    @GetMapping("/progress/overview")
    public ResponseEntity<List<PersonalRecordDto>> getPrOverview(Authentication authentication) {
        return ResponseEntity.ok(workoutLoggingService.getPrOverview(authentication.getName()));
    }

    /** Get 1RM progression chart data (daily best 1RM per exercise) */
    @GetMapping("/progress/chart/{exerciseName}")
    public ResponseEntity<List<Map<String, Object>>> getProgressionChart(
            @PathVariable String exerciseName,
            Authentication authentication) {
        return ResponseEntity.ok(workoutLoggingService.getProgressionChart(authentication.getName(), exerciseName));
    }

    // ========== Daily Nutrition Tips ==========

    /** Get current daily tip enabled status */
    @GetMapping("/tips/status")
    public ResponseEntity<Map<String, Object>> getTipStatus(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(Map.of(
                "dailyTipEnabled", user.getDailyTipEnabled() != null && user.getDailyTipEnabled(),
                "email", user.getEmail()
        ));
    }

    // ========== Quick Log (catch up on yesterday) ==========

    /**
     * Check if yesterday had any logged meals or workouts.
     * Returns what's missing so the dashboard can show quick-log prompts.
     */
    @GetMapping("/quick-log/status")
    public ResponseEntity<Map<String, Object>> getQuickLogStatus(Authentication authentication) {
        String email = authentication.getName();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<WorkoutSessionDto> yesterdaySessions = workoutLoggingService.getSessionsForDateRange(email, yesterday, yesterday);
        boolean hadWorkout = yesterdaySessions != null && yesterdaySessions.stream()
                .anyMatch(s -> Boolean.TRUE.equals(s.getCompleted()));

        Map<String, Object> yesterdayMeals = nutritionService.getDailyNutritionSummary(email, yesterday);
        int mealCount = yesterdayMeals.get("mealCount") instanceof Number n ? n.intValue() : 0;

        return ResponseEntity.ok(Map.of(
                "date", yesterday.toString(),
                "missedWorkout", !hadWorkout,
                "missedMeals", mealCount <= 0,
                "mealCount", mealCount
        ));
    }

    /** Quick-log a placeholder workout session for yesterday */
    @PostMapping("/quick-log/workout")
    public ResponseEntity<Map<String, Object>> quickLogWorkout(Authentication authentication) {
        String email = authentication.getName();
        WorkoutSessionDto session = workoutLoggingService.startSession(
                email, "Quick Catch-up", null
        );
        workoutLoggingService.completeSession(
                session.getId(), email, Map.of(
                        "durationMinutes", 0,
                        "caloriesBurned", 0,
                        "notes", "Quick-logged from dashboard — missed yesterday"
                )
        );
        return ResponseEntity.ok(Map.of(
                "message", "Workout logged for yesterday! 💪"
        ));
    }

    /** Quick-log a placeholder meal entry for yesterday */
    @PostMapping("/quick-log/meal")
    public ResponseEntity<Map<String, Object>> quickLogMeal(Authentication authentication) throws Exception {
        String email = authentication.getName();
        LogMealRequestDto dto = new LogMealRequestDto();
        dto.setMealType("SNACK");
        dto.setFoodName("Quick Catch-up Meal");
        dto.setDescription("Quick-logged from dashboard — missed yesterday");
        dto.setQuantity(1.0);
        dto.setUnit("meal");

        MealLogResponseDto meal = nutritionService.logMeal(dto, email);
        return ResponseEntity.ok(Map.of(
                "message", "Meal logged for yesterday! 🥗",
                "mealId", meal.getMealEntryId()
        ));
    }

    /** Toggle daily nutrition tips on/off */
    @PostMapping("/tips/toggle")
    public ResponseEntity<Map<String, Object>> toggleDailyTip(
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        boolean enabled = body.getOrDefault("enabled", !Boolean.TRUE.equals(user.getDailyTipEnabled()));
        userService.toggleDailyTip(user.getId(), enabled);
        return ResponseEntity.ok(Map.of(
                "dailyTipEnabled", enabled,
                "message", enabled ? "Daily nutrition tips enabled! You'll receive tips every morning." : "Daily nutrition tips disabled."
        ));
    }
}
