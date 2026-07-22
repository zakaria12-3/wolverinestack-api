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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReminderDispatchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderDispatchService.class);

    private final UserRepository userRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${APP_BASE_URL:http://localhost:4200}")
    private String baseUrl;

    public ReminderDispatchService(UserRepository userRepository,
                                   MealEntryRepository mealEntryRepository,
                                   WorkoutSessionRepository workoutSessionRepository,
                                   NotificationRepository notificationRepository,
                                   NotificationService notificationService,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
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
                        "🥗 Meal Reminder",
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
                        "💪 Workout Reminder",
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

        // Create in-app notification
        notificationService.create(member, type, title, message, link);

        // Also send a real email via Brevo
        try {
            if (member.getEmail() != null && !member.getEmail().isBlank()) {
                sendReminderEmail(member, type, title, message, link);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to send reminder email to {}: {}", member.getEmail(), e.getMessage());
        }

        LOGGER.info("Created {} for {}", type, member.getEmail());
    }

    private void sendReminderEmail(User member, String type, String title, String message, String link) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        String emoji = type.startsWith("WORKOUT") ? "💪" : "🥗";
        String subject = emoji + " " + title + " — " + dateStr;

        String html = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0D0D1A;">
        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0D0D1A;padding:30px 0;">
        <tr><td align="center">
        <table width="560" cellpadding="0" cellspacing="0" style="background:#1A1A2E;border-radius:16px;overflow:hidden;box-shadow:0 8px 32px rgba(0,0,0,0.3);border:1px solid #FFB800/20;">
        <tr>
        <td style="background:linear-gradient(135deg,#1A1A2E,#FFB800);padding:32px 40px;text-align:center;">
        <div style="font-size:48px;margin-bottom:8px;">%s</div>
        <h1 style="color:#ffffff;font-size:22px;margin:0;font-weight:700;font-family:'Archivo Narrow',sans-serif;">%s</h1>
        <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">%s</p>
        </td>
        </tr>
        <tr>
        <td style="padding:32px 40px;text-align:center;">
        <p style="color:#e5e2e1;font-size:16px;line-height:1.6;margin:0 0 24px;">%s</p>
        <a href="%s%s" style="display:inline-block;padding:14px 32px;background:#FFB800;color:#0D0D1A;text-decoration:none;border-radius:8px;font-weight:700;font-size:14px;">Open Wolverine Stack</a>
        <p style="color:#8f9378;font-size:12px;margin-top:24px;">
        You're receiving this reminder because you're a member of Wolverine Stack.
        <br>Log in to manage your notification preferences.
        </p>
        </td>
        </tr>
        </table>
        </td></tr>
        </table>
        </body>
        </html>
        """.formatted(emoji, title, dateStr, message, baseUrl, link);

        emailService.sendVerificationEmail(member.getEmail(), subject, html);
    }
}
