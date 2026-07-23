package com.example.controller;

import com.example.dto.ForgotPasswordRequestDto;
import com.example.dto.GoalSuggestionRequestDto;
import com.example.dto.LoginUserDto;
import com.example.dto.OnboardingRequestDto;
import com.example.dto.RegisterUserDto;
import com.example.dto.ResetPasswordDto;
import com.example.dto.VerifyPasswordResetCodeDto;
import com.example.dto.VerifyUserDto;
import com.example.dto.SignupResponseDto;
import com.example.model.User;
import com.example.response.LoginResponse;
import com.example.service.AuthenticationService;
import com.example.service.FitnessAIService;
import com.example.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final FitnessAIService fitnessAIService;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService,
                                    FitnessAIService fitnessAIService,
                                    JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.fitnessAIService = fitnessAIService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@Valid @RequestBody RegisterUserDto dto) {
        User user = authenticationService.signup(dto);
        boolean hasOnboarding = user.getGender() != null && user.getFitnessGoal() != null
                && user.getActivityLevel() != null
                && user.getWeightKg() != null && user.getHeightCm() != null
                && user.getDateOfBirth() != null;
        SignupResponseDto response = new SignupResponseDto(
                user.getId(),
                user.getRealUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getApprovalStatus(),
                hasOnboarding
        );
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Complete fitness onboarding — set goal, activity level, body metrics,
     * and optionally auto-calculate TDEE daily goals.
     * Requires authentication (user must be logged in).
     */
    /**
     * AI-powered goal suggestion: given basic metrics (gender, weight, height, DOB),
     * the AI recommends the most suitable fitness goals and activity levels.
     * The user can review these suggestions and then call POST /auth/onboarding to confirm.
     */
    @PostMapping("/onboarding/suggest-goals")
    public ResponseEntity<?> suggestGoals(
            @Valid @RequestBody GoalSuggestionRequestDto dto) {
        try {
            int age = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();

            // Calculate BMR server-side (LLMs are unreliable for arithmetic)
            double bmr = switch (dto.getGender().toUpperCase()) {
                case "FEMALE" -> 10 * dto.getWeightKg() + 6.25 * dto.getHeightCm() - 5 * age - 161;
                default      -> 10 * dto.getWeightKg() + 6.25 * dto.getHeightCm() - 5 * age + 5;
            };
            double tdeeModerate = Math.round(bmr * 1.55); // moderately active multiplier

            Map<String, Object> aiResult = fitnessAIService.suggestGoalsForMetrics(
                    dto.getGender(), dto.getWeightKg(), dto.getHeightCm(), age,
                    Math.round(bmr), Math.round(tdeeModerate)
            );

            if (aiResult.containsKey("error")) {
                return ResponseEntity.ok(Map.of(
                        "goals", List.of(
                                Map.of("value", "GENERAL_FITNESS", "label", "General Fitness", "confidence", 80, "reasoning", "A balanced starting point for most users."),
                                Map.of("value", "LOSE_WEIGHT", "label", "Lose Weight", "confidence", 60, "reasoning", "Common goal; adjust based on your personal preference."),
                                Map.of("value", "BUILD_MUSCLE", "label", "Build Muscle", "confidence", 50, "reasoning", "Great for body recomposition.")
                        ),
                        "activityLevels", List.of(
                                Map.of("value", "MODERATELY_ACTIVE", "label", "Moderately Active", "confidence", 70, "reasoning", "A good starting assumption for most people."),
                                Map.of("value", "LIGHTLY_ACTIVE", "label", "Lightly Active", "confidence", 60, "reasoning", "If you have a desk job, start here.")
                        ),
                        "bmrEstimate", "Calculating...",
                        "tdeeEstimate", "Calculating...",
                        "summary", "Start with General Fitness and adjust as you learn more about your preferences.",
                        "source", "fallback"
                ));
            }

            return ResponseEntity.ok(aiResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/onboarding")
    public ResponseEntity<?> completeOnboarding(
            @Valid @RequestBody OnboardingRequestDto dto,
            Authentication authentication) {
        User user = authenticationService.completeOnboarding(
                authentication.getName(), dto
        );
        return ResponseEntity.ok(Map.of(
                "message", "Onboarding complete! Your fitness profile has been set up.",
                "fitnessGoal", user.getFitnessGoal() != null ? user.getFitnessGoal().name() : null,
                "activityLevel", user.getActivityLevel() != null ? user.getActivityLevel().name() : null,
                "dailyCalorieGoal", user.getDailyCalorieGoal(),
                "dailyProteinGoal", user.getDailyProteinGoal(),
                "dailyCarbsGoal", user.getDailyCarbsGoal(),
                "dailyFatGoal", user.getDailyFatGoal()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginUserDto dto) {
        try {
            User user = authenticationService.authenticate(dto);
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(
                    new LoginResponse(token, jwtService.getExpirationTime())
            );
        } catch (RuntimeException e) {
            // Return generic error to prevent user enumeration
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody VerifyUserDto dto) {

        authenticationService.verifyUser(dto);

        return ResponseEntity.ok("Account verified!");

    }

    @PostMapping("/resend")
    public ResponseEntity<String> resend(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        authenticationService.resendVerificationCode(email);
        return ResponseEntity.ok("Verification code sent.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto dto) {
        authenticationService.requestPasswordReset(dto.getEmail());
        return ResponseEntity.ok("Password reset code sent.");
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<String> verifyPasswordResetCode(@Valid @RequestBody VerifyPasswordResetCodeDto dto) {
        authenticationService.verifyPasswordResetCode(dto.getEmail(), dto.getCode());
        return ResponseEntity.ok("Password reset code verified.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        authenticationService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return ResponseEntity.ok("Password changed successfully.");
    }
}
