package com.example.service;

import com.example.dto.*;
import com.example.model.*;
import com.example.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class WorkoutLoggingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutLoggingService.class);

    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutSessionExerciseRepository sessionExerciseRepository;
    private final ExerciseSetRepository exerciseSetRepository;
    private final WorkoutPlanRepository planRepository;
    private final UserRepository userRepository;

    public WorkoutLoggingService(WorkoutSessionRepository sessionRepository,
                                 WorkoutSessionExerciseRepository sessionExerciseRepository,
                                 ExerciseSetRepository exerciseSetRepository,
                                 WorkoutPlanRepository planRepository,
                                 UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.exerciseSetRepository = exerciseSetRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    // ========================================================================
    // SESSION MANAGEMENT
    // ========================================================================

    /** Start a new workout session (optionally from a plan) */
    public WorkoutSessionDto startSession(String email, String sessionName, Long planId) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkoutPlan plan = null;
        if (planId != null) {
            plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Workout plan not found"));
        }

        WorkoutSession session = new WorkoutSession();
        session.setSessionName(sessionName != null ? sessionName : "Workout " + LocalDateTime.now().toLocalDate());
        session.setMember(member);
        session.setWorkoutPlan(plan);
        session.setStartedAt(LocalDateTime.now());
        session.setCompleted(false);

        // If starting from a plan, pre-populate the session exercises (without sets)
        if (plan != null && plan.getExercises() != null) {
            for (WorkoutExercise ex : plan.getExercises()) {
                WorkoutSessionExercise sEx = new WorkoutSessionExercise();
                sEx.setSession(session);
                sEx.setExerciseName(ex.getExerciseName());
                sEx.setMuscleGroup(ex.getMuscleGroup());
                sEx.setEquipment(ex.getEquipment());
                sEx.setTargetSets(ex.getSets());
                sEx.setTargetReps(ex.getReps());
                sEx.setDurationSeconds(ex.getDurationSeconds());
                sEx.setRestSeconds(ex.getRestSeconds());
                sEx.setImageUrl(ex.getImageUrl());
                sEx.setOrderIndex(ex.getOrderIndex());
                session.getSessionExercises().add(sEx);
            }
        }

        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    /** Log an exercise into an active session (Hevy-style: creates the exercise shell, sets are added separately) */
    public WorkoutSessionDto addExerciseToSession(Long sessionId, String email, LogExerciseRequestDto request) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);

        WorkoutSessionExercise sEx = new WorkoutSessionExercise();
        sEx.setSession(session);
        sEx.setExerciseName(request.getExerciseName());
        sEx.setMuscleGroup(request.getMuscleGroup());
        sEx.setEquipment(request.getEquipment());
        sEx.setImageUrl(request.getImageUrl());
        sEx.setTargetSets(request.getTargetSets());
        sEx.setTargetReps(request.getTargetReps());
        sEx.setDurationSeconds(request.getDurationSeconds());
        sEx.setRestSeconds(request.getRestSeconds());
        sEx.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex()
                : session.getSessionExercises().size() + 1);
        sEx.setNotes(request.getNotes());

        session.getSessionExercises().add(sEx);
        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    /** Remove an exercise from an active session */
    public WorkoutSessionDto removeExerciseFromSession(Long sessionId, Long exerciseId, String email) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);
        session.getSessionExercises().removeIf(ex -> ex.getId().equals(exerciseId));
        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    // ========================================================================
    // PER-SET LOGGING (Hevy-style core)
    // ========================================================================

    /** Log a single set to a session exercise */
    public SessionExerciseDto logSet(Long sessionId, Long exerciseId, String email, LogSetRequestDto request) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);

        WorkoutSessionExercise sEx = session.getSessionExercises().stream()
                .filter(ex -> ex.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Exercise not found in session"));

        ExerciseSet set = new ExerciseSet();
        set.setSessionExercise(sEx);
        set.setSetIndex(sEx.getSets().size() + 1);
        set.setSetType(request.getSetType() != null
                ? ExerciseSet.SetType.valueOf(request.getSetType())
                : ExerciseSet.SetType.NORMAL);
        set.setWeightKg(request.getWeightKg());
        set.setReps(request.getReps());
        set.setRpe(request.getRpe());
        set.setDurationSeconds(request.getDurationSeconds());
        set.setDistanceMeters(request.getDistanceMeters());
        set.setNotes(request.getNotes());

        sEx.getSets().add(set);
        sessionRepository.save(session);

        // Check for PR
        ExerciseSet savedSet = set;
        ExerciseSetDto dto = toSetDto(savedSet);
        checkAndSetPr(dto, sEx.getExerciseName(), session.getMember().getId());
        return toSessionExerciseDto(sEx);
    }

    /** Update an existing set (weight, reps, RPE, etc.) */
    public SessionExerciseDto updateSet(Long sessionId, Long exerciseId, Long setId,
                                         String email, LogSetRequestDto request) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);
        ExerciseSet set = exerciseSetRepository.findById(setId)
                .orElseThrow(() -> new RuntimeException("Set not found"));

        if (request.getSetType() != null)
            set.setSetType(ExerciseSet.SetType.valueOf(request.getSetType()));
        if (request.getWeightKg() != null)
            set.setWeightKg(request.getWeightKg());
        if (request.getReps() != null)
            set.setReps(request.getReps());
        if (request.getRpe() != null)
            set.setRpe(request.getRpe());
        if (request.getDurationSeconds() != null)
            set.setDurationSeconds(request.getDurationSeconds());
        if (request.getDistanceMeters() != null)
            set.setDistanceMeters(request.getDistanceMeters());
        if (request.getNotes() != null)
            set.setNotes(request.getNotes());

        exerciseSetRepository.save(set);

        WorkoutSessionExercise sEx = session.getSessionExercises().stream()
                .filter(ex -> ex.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Exercise not found in session"));

        return toSessionExerciseDto(sEx);
    }

    /** Delete a single set */
    public SessionExerciseDto deleteSet(Long sessionId, Long exerciseId, Long setId, String email) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);

        WorkoutSessionExercise sEx = session.getSessionExercises().stream()
                .filter(ex -> ex.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Exercise not found in session"));

        sEx.getSets().removeIf(s -> s.getId().equals(setId));
        // Re-index remaining sets
        for (int i = 0; i < sEx.getSets().size(); i++) {
            sEx.getSets().get(i).setSetIndex(i + 1);
        }

        sessionRepository.save(session);
        return toSessionExerciseDto(sEx);
    }

    /** Quick-log: add a complete set of sets for an exercise (for fast logging) */
    public SessionExerciseDto bulkLogSets(Long sessionId, Long exerciseId, String email,
                                           List<LogSetRequestDto> requests) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);

        WorkoutSessionExercise sEx = session.getSessionExercises().stream()
                .filter(ex -> ex.getId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Exercise not found in session"));

        int nextIndex = sEx.getSets().size() + 1;
        for (LogSetRequestDto req : requests) {
            ExerciseSet set = new ExerciseSet();
            set.setSessionExercise(sEx);
            set.setSetIndex(nextIndex++);
            set.setSetType(req.getSetType() != null
                    ? ExerciseSet.SetType.valueOf(req.getSetType())
                    : ExerciseSet.SetType.NORMAL);
            set.setWeightKg(req.getWeightKg());
            set.setReps(req.getReps());
            set.setRpe(req.getRpe());
            set.setDurationSeconds(req.getDurationSeconds());
            set.setDistanceMeters(req.getDistanceMeters());
            set.setNotes(req.getNotes());
            sEx.getSets().add(set);
        }

        sessionRepository.save(session);
        return toSessionExerciseDto(sEx);
    }

    // ========================================================================
    // SESSION COMPLETION
    // ========================================================================

    /** Complete a workout session */
    public WorkoutSessionDto completeSession(Long sessionId, String email, Map<String, Object> completionData) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);

        session.setCompleted(true);
        session.setCompletedAt(LocalDateTime.now());

        if (completionData != null) {
            if (completionData.get("durationMinutes") instanceof Number n)
                session.setDurationMinutes(n.intValue());
            if (completionData.get("caloriesBurned") instanceof Number n)
                session.setCaloriesBurned(n.intValue());
            if (completionData.get("intensity") instanceof String s)
                session.setIntensity(s);
            if (completionData.get("notes") instanceof String s)
                session.setNotes(s);
            if (completionData.get("rating") instanceof Number n)
                session.setRating(n.intValue());
        }

        if (session.getDurationMinutes() == null && session.getStartedAt() != null) {
            long minutes = java.time.Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();
            session.setDurationMinutes(Math.max(1, (int) minutes));
        }

        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    // ========================================================================
    // QUERIES
    // ========================================================================

    /** Get all sessions for the current member */
    public List<WorkoutSessionDto> getMySessions(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return sessionRepository.findByMemberIdOrderByStartedAtDesc(member.getId())
                .stream()
                .map(this::toSessionDto)
                .toList();
    }

    /** Get a single session with details */
    public WorkoutSessionDto getSession(Long sessionId, String email) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);
        return toSessionDto(session);
    }

    /** Get sessions for a date range */
    public List<WorkoutSessionDto> getSessionsForDateRange(String email, LocalDate start, LocalDate end) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return sessionRepository.findByMemberIdAndStartedAtBetweenOrderByStartedAtAsc(
                        member.getId(),
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                ).stream()
                .map(this::toSessionDto)
                .toList();
    }

    /** Delete a session */
    public void deleteSession(Long sessionId, String email) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);
        sessionRepository.delete(session);
    }

    // ========================================================================
    // PROGRESS & PR TRACKING
    // ========================================================================

    /** Get progress data for a specific exercise */
    public ProgressDataDto getProgress(String email, String exerciseName) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> rawData = sessionRepository.findProgressForExercise(member.getId(), exerciseName);

        ProgressDataDto result = new ProgressDataDto();
        result.setExerciseName(exerciseName);
        result.setProgressPoints(new ArrayList<>());

        for (Object[] row : rawData) {
            ProgressDataDto.ProgressPoint point = new ProgressDataDto.ProgressPoint();
            if (row[1] instanceof java.sql.Timestamp ts)
                point.setDate(ts.toLocalDateTime().toLocalDate());
            if (row[2] instanceof Number n) point.setWeightKg(n.doubleValue());
            if (row[3] instanceof Number n) point.setReps(n.intValue());
            if (row[4] instanceof Number n) point.setSets(n.intValue());

            if (point.getWeightKg() != null && point.getReps() != null && point.getReps() > 0) {
                int estimatedOneRm = (int) Math.round(point.getWeightKg() * (1.0 + point.getReps() / 30.0));
                point.setEstimatedOneRm(estimatedOneRm);
            }
            result.getProgressPoints().add(point);
        }
        return result;
    }

    /** Get all tracked exercises for a member */
    public List<String> getTrackedExercises(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return sessionRepository.findTrackedExercisesByMember(member.getId());
    }

    /** Get the previous best set (by 1RM estimate) for an exercise */
    private ExerciseSetDto getPreviousBest(String exerciseName, Long memberId) {
        List<Object[]> rawData = sessionRepository.findProgressForExercise(memberId, exerciseName);
        if (rawData.isEmpty()) return null;

        double best1RM = 0;
        ExerciseSetDto best = null;

        for (Object[] row : rawData) {
            Double weight = row[2] instanceof Number n ? n.doubleValue() : null;
            Integer reps = row[3] instanceof Number n ? n.intValue() : null;
            if (weight == null || reps == null || reps <= 0) continue;

            double e1rm = weight * (1.0 + reps / 30.0);
            if (e1rm > best1RM) {
                best1RM = e1rm;
                best = new ExerciseSetDto();
                best.setPreviousBestWeight(weight);
                best.setPreviousBestReps(reps);
            }
        }
        return best;
    }

    private void checkAndSetPr(ExerciseSetDto dto, String exerciseName, Long memberId) {
        if (dto.getWeightKg() == null || dto.getReps() == null || dto.getReps() <= 0) return;

        ExerciseSetDto previousBest = getPreviousBest(exerciseName, memberId);
        if (previousBest == null) {
            dto.setIsPersonalRecord(true);
            return;
        }

        double current1RM = dto.getWeightKg() * (1.0 + dto.getReps() / 30.0);
        double previous1RM = previousBest.getPreviousBestWeight() * (1.0 + previousBest.getPreviousBestReps() / 30.0);

        dto.setIsPersonalRecord(current1RM >= previous1RM);
        dto.setPreviousBestWeight(previousBest.getPreviousBestWeight());
        dto.setPreviousBestReps(previousBest.getPreviousBestReps());
    }

    /** Get all-time best PRs for every exercise the member has logged */
    public List<PersonalRecordDto> getPrOverview(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> rawData = sessionRepository.findAllTimeBestPerExercise(member.getId());
        List<PersonalRecordDto> results = new ArrayList<>();

        for (Object[] row : rawData) {
            PersonalRecordDto dto = new PersonalRecordDto();
            dto.setExerciseName(row[0] != null ? row[0].toString() : "Unknown");
            dto.setMuscleGroup(row[1] != null ? row[1].toString() : null);
            dto.setEquipment(row[2] != null ? row[2].toString() : null);
            if (row[3] instanceof Number n) dto.setBestWeightKg(n.doubleValue());
            if (row[4] instanceof Number n) dto.setBestReps(n.intValue());
            if (row[5] instanceof Number n) dto.setEstimatedOneRm(n.intValue());
            if (row[6] instanceof java.sql.Timestamp ts) dto.setAchievedDate(ts.toLocalDateTime().toLocalDate());
            if (row[7] instanceof Number n) dto.setSessionId(n.longValue());
            if (row[8] != null) dto.setSessionName(row[8].toString());
            if (row[9] instanceof Number n) dto.setTotalSessionsLogged(n.intValue());
            if (row[10] instanceof Number n) dto.setTotalSetsLogged(n.intValue());
            results.add(dto);
        }

        return results;
    }

    /**
     * Find exercises where the best set (by 1RM) was achieved more than 30 days ago,
     * and generate AI suggestions for breaking through the plateau.
     */
    public List<StalledLiftDto> getStalledLifts(String email, AIService aiService) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        List<Object[]> rawData = sessionRepository.findStalledExercises(
                member.getId(), cutoffDate.toString()
        );

        List<StalledLiftDto> results = new ArrayList<>();
        for (Object[] row : rawData) {
            StalledLiftDto dto = new StalledLiftDto();
            dto.setExerciseName(row[0] != null ? row[0].toString() : "Unknown");
            dto.setMuscleGroup(row[1] != null ? row[1].toString() : null);
            dto.setEquipment(row[2] != null ? row[2].toString() : null);
            if (row[3] instanceof Number n) dto.setBestWeightKg(n.doubleValue());
            if (row[4] instanceof Number n) dto.setBestReps(n.intValue());
            if (row[5] instanceof Number n) dto.setEstimatedOneRm(n.intValue());
            if (row[6] instanceof java.sql.Date d) dto.setBestAchievedDate(d.toLocalDate());
            if (row[7] instanceof Number n) dto.setDaysSinceImprovement(n.longValue());
            if (row[8] instanceof Number n) dto.setTotalSessionsLogged(n.intValue());
            if (row[9] instanceof Number n) dto.setTotalSetsLogged(n.intValue());

            // Generate AI suggestion for breaking this plateau
            dto.setAiSuggestion(generateStallSuggestion(dto, aiService));
            results.add(dto);
        }

        return results;
    }

    private String generateStallSuggestion(StalledLiftDto dto, AIService aiService) {
        if (!aiService.hasConfiguredApiKey()) {
            return "Try a different rep scheme (e.g., 5×5 heavy, or 3×8−10 with increased volume) or swap to a variation like " + dto.getExerciseName().replace("Barbell ", "Dumbbell ").replace(" ", "-") + " for 4 weeks to break the plateau.";
        }

        String prompt = String.format(
                """
                You are an expert strength coach analyzing a client's stalled lift.
                The client has not improved their best set for %s in %d days.

                Current best: %.0f kg × %d reps (estimated 1RM: %d kg)
                Muscle group: %s
                Equipment: %s
                Total sessions logged for this exercise: %d

                Provide ONE concise, actionable suggestion (max 2 sentences) for breaking this plateau.
                Focus on: rep scheme changes, tempo work, accessory exercises, or exercise variations.
                Be specific with numbers (sets × reps, percentages).
                Do not use markdown. Just plain text.
                """,
                dto.getExerciseName(), dto.getDaysSinceImprovement(),
                dto.getBestWeightKg() != null ? dto.getBestWeightKg() : 0,
                dto.getBestReps() != null ? dto.getBestReps() : 0,
                dto.getEstimatedOneRm() != null ? dto.getEstimatedOneRm() : 0,
                dto.getMuscleGroup() != null ? dto.getMuscleGroup() : "General",
                dto.getEquipment() != null ? dto.getEquipment() : "Bodyweight",
                dto.getTotalSessionsLogged()
        );

        try {
            String response = aiService.askAI(prompt);
            if (response != null && !response.startsWith("AI error")) {
                return response.trim();
            }
        } catch (Exception e) {
            // Fall through to fallback
        }

        // Fallback suggestion
        return "Try 3×5 at 85-90% of your 1RM with longer rest (3-4 min), or substitute with " + dto.getExerciseName() + " variation. Focus on the eccentric phase (3-4s lowering).";
    }

    /** Get 1RM progression data points for an exercise chart */
    public List<Map<String, Object>> getProgressionChart(String email, String exerciseName) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Object[]> rawData = sessionRepository.findProgressionForExerciseChart(member.getId(), exerciseName);
        List<Map<String, Object>> points = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> point = new HashMap<>();
            point.put("exerciseName", row[0] != null ? row[0].toString() : "Unknown");
            if (row[1] instanceof java.sql.Date d) point.put("date", d.toLocalDate().toString());
            if (row[2] instanceof Number n) point.put("estimatedOneRm", n.intValue());
            points.add(point);
        }

        return points;
    }

    /** Get workout summary stats */
    public Map<String, Object> getWorkoutSummary(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalSessions = sessionRepository.countByMemberIdAndCompletedTrue(member.getId());
        List<WorkoutSession> recentSessions = sessionRepository
                .findByMemberIdAndCompletedTrueOrderByStartedAtDesc(member.getId());

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long sessionsThisWeek = recentSessions.stream()
                .filter(s -> s.getStartedAt() != null && s.getStartedAt().isAfter(weekAgo))
                .count();

        int totalCaloriesThisWeek = recentSessions.stream()
                .filter(s -> s.getStartedAt() != null && s.getStartedAt().isAfter(weekAgo))
                .filter(s -> s.getCaloriesBurned() != null)
                .mapToInt(WorkoutSession::getCaloriesBurned)
                .sum();

        int totalMinutesThisWeek = recentSessions.stream()
                .filter(s -> s.getStartedAt() != null && s.getStartedAt().isAfter(weekAgo))
                .filter(s -> s.getDurationMinutes() != null)
                .mapToInt(WorkoutSession::getDurationMinutes)
                .sum();

        return Map.of(
                "totalSessions", totalSessions,
                "sessionsThisWeek", sessionsThisWeek,
                "totalCaloriesBurnedThisWeek", totalCaloriesThisWeek,
                "totalMinutesThisWeek", totalMinutesThisWeek
        );
    }

    // ========================================================================
    // DTO CONVERSION
    // ========================================================================

    private WorkoutSessionDto toSessionDto(WorkoutSession session) {
        WorkoutSessionDto dto = new WorkoutSessionDto();
        dto.setId(session.getId());
        dto.setSessionName(session.getSessionName());
        dto.setStartedAt(session.getStartedAt());
        dto.setCompletedAt(session.getCompletedAt());
        dto.setCompleted(session.getCompleted());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setCaloriesBurned(session.getCaloriesBurned());
        dto.setIntensity(session.getIntensity());
        dto.setNotes(session.getNotes());
        dto.setRating(session.getRating());

        if (session.getWorkoutPlan() != null) {
            dto.setPlanId(session.getWorkoutPlan().getId());
            dto.setPlanName(session.getWorkoutPlan().getName());
        }

        if (session.getSessionExercises() != null) {
            dto.setTotalExercises(session.getSessionExercises().size());
            dto.setCompletedExercises((int) session.getSessionExercises().stream()
                    .filter(ex -> ex.getCompletedSetCount() > 0)
                    .count());
            dto.setSessionExercises(session.getSessionExercises().stream()
                    .map(this::toSessionExerciseDto)
                    .toList());
        }

        return dto;
    }

    private SessionExerciseDto toSessionExerciseDto(WorkoutSessionExercise ex) {
        SessionExerciseDto dto = new SessionExerciseDto();
        dto.setId(ex.getId());
        dto.setExerciseName(ex.getExerciseName());
        dto.setMuscleGroup(ex.getMuscleGroup());
        dto.setEquipment(ex.getEquipment());
        dto.setImageUrl(ex.getImageUrl());
        dto.setTargetSets(ex.getTargetSets());
        dto.setTargetReps(ex.getTargetReps());
        dto.setCompletedSetCount(ex.getCompletedSetCount());
        dto.setTotalReps(ex.getTotalReps());
        dto.setTotalVolume(ex.getTotalVolume());
        dto.setDurationSeconds(ex.getDurationSeconds());
        dto.setRestSeconds(ex.getRestSeconds());
        dto.setOrderIndex(ex.getOrderIndex());
        dto.setNotes(ex.getNotes());
        dto.setLoggedAt(ex.getLoggedAt());

        if (ex.getSets() != null) {
            dto.setSets(ex.getSets().stream()
                    .map(this::toSetDto)
                    .toList());
        }

        // Check for previous best for PR indicator
        if (ex.getSession() != null && ex.getSession().getMember() != null) {
            Long memberId = ex.getSession().getMember().getId();
            ExerciseSetDto previousBest = getPreviousBest(ex.getExerciseName(), memberId);
            if (previousBest != null) {
                dto.setPreviousBestWeight(previousBest.getPreviousBestWeight());
                dto.setPreviousBestReps(previousBest.getPreviousBestReps());
            }
        }

        return dto;
    }

    private ExerciseSetDto toSetDto(ExerciseSet set) {
        ExerciseSetDto dto = new ExerciseSetDto();
        dto.setId(set.getId());
        dto.setSetIndex(set.getSetIndex());
        dto.setSetType(set.getSetType().name());
        dto.setWeightKg(set.getWeightKg());
        dto.setReps(set.getReps());
        dto.setRpe(set.getRpe());
        dto.setDurationSeconds(set.getDurationSeconds());
        dto.setDistanceMeters(set.getDistanceMeters());
        dto.setNotes(set.getNotes());
        dto.setLoggedAt(set.getLoggedAt());
        return dto;
    }

    private WorkoutSession validateSessionOwnership(Long sessionId, String email) {
        WorkoutSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found"));
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!session.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("Not allowed to access this workout session");
        }
        return session;
    }
}
