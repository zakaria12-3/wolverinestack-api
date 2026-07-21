package com.example.service;

import com.example.model.ReportTargetType;
import com.example.model.User;
import com.example.model.Workout;
import com.example.repository.ReportRepository;
import com.example.repository.UserRepository;
import com.example.repository.WorkoutRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ModerationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModerationService.class);

    private static final Pattern SCAM_PATTERN = Pattern.compile("(?i)(travail facile|gagne\\s*\\d+|sans exp[eé]rience|argent facile(?:ment)?|revenu garanti|crypto|investissement|telegram|whatsapp|western union|moneygram|frais d[' ]?inscription|paiement avant|commission uniquement)");
    private static final Pattern PHISHING_PATTERN = Pattern.compile("(?i)(http://|https://|bit\\.ly|tinyurl|t\\.me/|wa\\.me/)[^\\s]*");
    private static final Pattern TOXIC_PATTERN = Pattern.compile("(?i)(arnaque|fraud|harc[eè]lement|toxic|hate|haine|violence|menace|humiliation|exploitation)");
    private static final Pattern PERSONAL_DATA_PATTERN = Pattern.compile("(?i)(envoyez.*(cin|passport|passeport|rib|carte bancaire)|numero de carte|num[eé]ro de carte|mot de passe|code bancaire)");
    private static final Pattern DISCRIMINATION_PATTERN = Pattern.compile("(?i)(homme uniquement|femme uniquement|moins de \\d+ ans|nationalit[eé] exig[eé]e|religion|enceinte|handicap)");

    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final ReportRepository reportRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ModerationService(UserRepository userRepository,
                             WorkoutRepository workoutRepository,
                             ReportRepository reportRepository,
                             AIService aiService) {
        this.userRepository = userRepository;
        this.workoutRepository = workoutRepository;
        this.reportRepository = reportRepository;
        this.aiService = aiService;
    }

    public void evaluateUser(User user) {
        int score = 0;

        long reportCount = reportRepository.countByTargetTypeAndTargetId(ReportTargetType.USER, user.getId());
        score += (int) reportCount;

        if (user.getBio() == null || user.getBio().isBlank()) score += 1;
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) score += 1;

        if ("REJECTED".equalsIgnoreCase(user.getApprovalStatus())) score += 10;
        else if ("PENDING".equalsIgnoreCase(user.getApprovalStatus())) score += 2;

        user.setRiskScore(score);

        if (score >= 15) {
            user.setSuspended(true);
            user.setSuspensionReason("Automatic suspension due to high risk score (" + score + ")");
            user.setReported(true);
        } else if (score >= 10) {
            user.setReported(true);
            user.setSuspended(false);
            user.setSuspensionReason(null);
        } else {
            user.setReported(false);
            user.setSuspended(false);
            user.setSuspensionReason(null);
        }

        userRepository.save(user);
        LOGGER.info("Evaluated user {}, new risk score: {}", user.getEmail(), score);
    }

    public void evaluateWorkout(com.example.model.Workout workout) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        String text = String.join(" ",
                safe(workout.getName()),
                safe(workout.getDescription()),
                safe(workout.getInstructions()),
                safe(workout.getMuscleGroup())
        ).toLowerCase();

        if (SCAM_PATTERN.matcher(text).find()) {
            score += 5;
            reasons.add("Scam-like wording or unrealistic promises");
        }
        if (TOXIC_PATTERN.matcher(text).find()) {
            score += 3;
            reasons.add("Harmful, abusive, or fraudulent wording");
        }
        if (PHISHING_PATTERN.matcher(text).find()) {
            score += 3;
            reasons.add("Suspicious external contact or link");
        }
        if (PERSONAL_DATA_PATTERN.matcher(text).find()) {
            score += 5;
            reasons.add("Requests sensitive identity or banking data");
        }
        if (DISCRIMINATION_PATTERN.matcher(text).find()) {
            score += 4;
            reasons.add("Potentially discriminatory requirement");
        }

        long emojiCount = text.codePoints().filter(c -> c >= 0x1F300 && c <= 0x1FAFF).count();
        if (emojiCount > 3) {
            score += 2;
            reasons.add("Spam-like emoji usage");
        }

        if (workout.getId() != null) {
            long duplicates = workoutRepository.countByNameAndDescription(workout.getName(), workout.getDescription());
            if (duplicates > 1) {
                score += 3;
                reasons.add("Duplicate workout content");
            }
        }

        if (workout.getTrainer() != null && !"APPROVED".equalsIgnoreCase(workout.getTrainer().getApprovalStatus())) {
            score += 4;
            reasons.add("Trainer account is not fully approved");
        }

        workout.setRiskScore(score);
        workout.setSuspicious(score >= 7);
        workout.setModerationStatus(score >= 10 ? "BLOCKED" : score >= 7 ? "REVIEW" : "APPROVED");
        workout.setModerationReason(reasons.isEmpty() ? null : String.join("; ", reasons));

        LOGGER.info("Evaluated workout '{}', risk score {}, status {}", workout.getName(), score, workout.getModerationStatus());
    }

    public void approveWorkout(com.example.model.Workout workout) {
        workout.setRiskScore(0);
        workout.setSuspicious(false);
        workout.setModerationStatus("APPROVED");
        workout.setModerationReason("Approved by admin review");
    }

    public void rejectWorkout(com.example.model.Workout workout, String reason) {
        workout.setActive(false);
        workout.setSuspicious(true);
        workout.setModerationStatus("BLOCKED");
        workout.setModerationReason(reason == null || reason.isBlank() ? "Blocked by admin review" : reason.trim());
    }

    private String safe(String value) {
        if (value == null) return "";
        return value.length() > 2500 ? value.substring(0, 2500) : value;
    }

    private record AiModerationResult(int score, List<String> reasons) {
    }
}
