package com.example.service;

import com.example.dto.*;
import com.example.model.*;
import com.example.repository.UserRepository;
import com.example.repository.WorkoutPlanRepository;
import com.example.repository.WorkoutSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WorkoutLoggingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkoutLoggingService.class);

    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutPlanRepository planRepository;
    private final UserRepository userRepository;

    public WorkoutLoggingService(WorkoutSessionRepository sessionRepository,
                                 WorkoutPlanRepository planRepository,
                                 UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

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

        // If starting from a plan, pre-populate the exercises
        if (plan != null && plan.getExercises() != null) {
            for (WorkoutExercise ex : plan.getExercises()) {
                WorkoutLogEntry entry = new WorkoutLogEntry();
                entry.setSession(session);
                entry.setExerciseName(ex.getExerciseName());
                entry.setMuscleGroup(ex.getMuscleGroup());
                entry.setEquipment(ex.getEquipment());
                entry.setTargetSets(ex.getSets());
                entry.setTargetReps(ex.getReps());
                entry.setDurationSeconds(ex.getDurationSeconds());
                entry.setRestSeconds(ex.getRestSeconds());
                entry.setOrderIndex(ex.getOrderIndex());
                session.getLogEntries().add(entry);
            }
        }

        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

    /** Log an exercise within an active session */
    public WorkoutSessionDto logExercise(Long sessionId, String email, LogExerciseRequestDto request) {
        WorkoutSession session = validateSessionOwnership(sessionId, email);

        WorkoutLogEntry entry = new WorkoutLogEntry();
        entry.setSession(session);
        entry.setExerciseName(request.getExerciseName());
        entry.setMuscleGroup(request.getMuscleGroup());
        entry.setEquipment(request.getEquipment());
        entry.setTargetSets(request.getTargetSets());
        entry.setTargetReps(request.getTargetReps());
        entry.setActualSets(request.getActualSets());
        entry.setActualReps(request.getActualReps());
        entry.setWeightKg(request.getWeightKg());
        entry.setDurationSeconds(request.getDurationSeconds());
        entry.setRestSeconds(request.getRestSeconds());
        entry.setOrderIndex(request.getOrderIndex());
        entry.setNotes(request.getNotes());

        session.getLogEntries().add(entry);
        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

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

        // Calculate duration if not provided
        if (session.getDurationMinutes() == null && session.getStartedAt() != null) {
            long minutes = java.time.Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();
            session.setDurationMinutes(Math.max(1, (int) minutes));
        }

        WorkoutSession saved = sessionRepository.save(session);
        return toSessionDto(saved);
    }

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
            // row[0] is exerciseName (same for all points, set on the DTO)
            if (row[1] instanceof java.sql.Timestamp ts)
                point.setDate(ts.toLocalDateTime().toLocalDate());
            if (row[2] instanceof Number n) point.setWeightKg(n.doubleValue());
            if (row[3] instanceof Number n) point.setReps(n.intValue());
            if (row[4] instanceof Number n) point.setSets(n.intValue());

            // Estimate 1RM using Epley formula: weight * (1 + reps/30)
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

    /** Get summary stats for the member */
    public Map<String, Object> getWorkoutSummary(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalSessions = sessionRepository.countByMemberIdAndCompletedTrue(member.getId());
        List<WorkoutSession> recentSessions = sessionRepository
                .findByMemberIdAndCompletedTrueOrderByStartedAtDesc(member.getId());

        // Weekly stats
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

    // --- Helper methods ---

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

        if (session.getLogEntries() != null) {
            dto.setTotalExercises(session.getLogEntries().size());
            dto.setCompletedExercises((int) session.getLogEntries().stream()
                    .filter(e -> e.getActualSets() != null || e.getActualReps() != null)
                    .count());
            dto.setLogEntries(session.getLogEntries().stream()
                    .map(this::toLogEntryDto)
                    .toList());
        }

        return dto;
    }

    private WorkoutLogEntryDto toLogEntryDto(WorkoutLogEntry entry) {
        WorkoutLogEntryDto dto = new WorkoutLogEntryDto();
        dto.setId(entry.getId());
        dto.setExerciseName(entry.getExerciseName());
        dto.setMuscleGroup(entry.getMuscleGroup());
        dto.setEquipment(entry.getEquipment());
        dto.setTargetSets(entry.getTargetSets());
        dto.setTargetReps(entry.getTargetReps());
        dto.setActualSets(entry.getActualSets());
        dto.setActualReps(entry.getActualReps());
        dto.setWeightKg(entry.getWeightKg());
        dto.setDurationSeconds(entry.getDurationSeconds());
        dto.setRestSeconds(entry.getRestSeconds());
        dto.setOrderIndex(entry.getOrderIndex());
        dto.setNotes(entry.getNotes());
        dto.setLoggedAt(entry.getLoggedAt());
        return dto;
    }
}
