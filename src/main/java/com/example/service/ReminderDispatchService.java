package com.example.service;

import com.example.model.MealEntry;
import com.example.model.Role;
import com.example.model.User;
import com.example.model.WorkoutSession;
import com.example.repository.MealEntryRepository;
import com.example.repository.NotificationRepository;
import com.example.repository.UserRepository;
import com.example.repository.WorkoutSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReminderDispatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderDispatchService.class);

    private final UserRepository userRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public ReminderDispatchService(UserRepository userRepository,
                                   MealEntryRepository mealEntryRepository,
                                   WorkoutSessionRepository workoutSessionRepository,
                                   NotificationRepository notificationRepository,
                                   NotificationService notificationService) {
        this.userRepository = userRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 9,13,19 * * *")
    @Transactional
    public void remindMembersToLogMeals() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        List<User> members = userRepository.findByRole(Role.ROLE_MEMBER);

        for (User member : members) {
            if (!member.isEmailVerified()) {
                continue;
            }

            List<MealEntry> todaysMeals = mealEntryRepository
                    .findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(member.getId(), start, end);

            if (todaysMeals.isEmpty()) {
                createReminderOncePerDay(
                        member,
                        "MEAL_REMINDER",
                        "Meal reminder",
                        "You have not logged a meal today. Add breakfast, lunch, dinner, or a quick snack to keep your nutrition accurate.",
                        "/member/nutrition",
                        start,
                        end
                );
            }
        }
    }

    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void remindMembersToTrain() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        List<User> members = userRepository.findByRole(Role.ROLE_MEMBER);

        for (User member : members) {
            if (!member.isEmailVerified()) {
                continue;
            }

            List<WorkoutSession> todaysSessions = workoutSessionRepository
                    .findByMemberIdAndStartedAtBetweenOrderByStartedAtAsc(member.getId(), start, end);

            boolean completedWorkout = todaysSessions.stream()
                    .anyMatch(session -> Boolean.TRUE.equals(session.getCompleted()));

            if (!completedWorkout) {
                createReminderOncePerDay(
                        member,
                        "WORKOUT_REMINDER",
                        "Workout reminder",
                        "You have not completed a workout today. Start a session or pick a plan when you are ready.",
                        "/member/workouts",
                        start,
                        end
                );
            }
        }
    }

    private void createReminderOncePerDay(User member,
                                          String type,
                                          String title,
                                          String message,
                                          String link,
                                          LocalDateTime start,
                                          LocalDateTime end) {
        boolean alreadySent = notificationRepository
                .existsByRecipientIdAndTypeAndCreatedAtBetween(member.getId(), type, start, end);

        if (alreadySent) {
            return;
        }

        notificationService.create(member, type, title, message, link);
        LOGGER.info("Created {} for {}", type, member.getEmail());
    }
}
