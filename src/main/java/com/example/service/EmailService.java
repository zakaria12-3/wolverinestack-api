package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${BREVO_API_KEY:}")
    private String apiKey;

    @Value("${SUPPORT_EMAIL:}")
    private String senderEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private void sendEmail(String to, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            LOGGER.warn("Email not sent because BREVO_API_KEY or SUPPORT_EMAIL is not configured");
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                "sender",      Map.of("email", senderEmail),
                "to",          List.of(Map.of("email", to)),
                "subject",     subject,
                "htmlContent", htmlContent
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                LOGGER.error("Brevo email error {} for {}: {}", response.statusCode(), to, response.body());
                return;
            }

        } catch (Exception e) {
            LOGGER.error("Failed to send email to {}", to, e);
        }
    }

    public void sendVerificationEmail(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String to, String subject, String htmlContent) {
        sendEmail(to, subject, htmlContent);
    }

    public void sendApplicationResultEmail(String to, String status, String jobTitle) {
        boolean accepted = "FINAL_ACCEPTED".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status);
        String subject = "Your Job Application Status";
        String html = "<html><body style='font-family: Arial;'>"
                + "<h2>Application Update</h2>"
                + "<p>Your application for <b>" + jobTitle + "</b> has been <b>" + (accepted ? "accepted" : "rejected") + "</b>.</p>"
                + (accepted
                    ? "<p style='color:green;'>Congratulations! You have been selected!</p>"
                    : "<p style='color:red;'>We regret to inform you that you were not selected.</p>")
                + "</body></html>";
        try {
            sendEmail(to, subject, html);
        } catch (IllegalStateException e) {
            LOGGER.error("Application result email was not sent to {}", to, e);
        }
    }

    public void sendInterviewEmail(String to, String jobTitle, String link, java.time.LocalDateTime interviewDate) {
        String subject = "Interview Invitation";
        String dateText = interviewDate != null 
                ? "<p>Date & Time: <b>" + java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(interviewDate) + "</b></p>"
                : "";
        String html = "<h2>Interview Scheduled</h2>"
                + "<p>Your application for <b>" + jobTitle + "</b> was selected for an interview.</p>"
                + dateText
                + "<p>Join the meeting here:</p>"
                + "<a href='" + link + "'>" + link + "</a>";
        try {
            sendEmail(to, subject, html);
        } catch (IllegalStateException e) {
            LOGGER.error("Interview email was not sent to {}", to, e);
        }
    }

    public void sendDailyNutritionTipEmail(String to, String username, String tip, String focusArea,
                                              String suggestion, String encouragement,
                                              int totalCalories, int goalCalories,
                                              double totalProtein, int goalProtein,
                                              double totalCarbs, int goalCarbs,
                                              double totalFat, int goalFat) {
        String subject = "🌱 Your Daily Nutrition Tip – " + java.time.LocalDate.now();

        double calPct = goalCalories > 0 ? Math.min(100.0, totalCalories * 100.0 / goalCalories) : 0;
        double protPct = goalProtein > 0 ? Math.min(100.0, totalProtein * 100.0 / goalProtein) : 0;
        double carbPct = goalCarbs > 0 ? Math.min(100.0, totalCarbs * 100.0 / goalCarbs) : 0;
        double fatPct = goalFat > 0 ? Math.min(100.0, totalFat * 100.0 / goalFat) : 0;

        String barColor = calPct >= 90 && calPct <= 110 ? "#22c55e" : calPct > 110 ? "#ef4444" : "#f59e0b";

        String html = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#f8fafc;">
        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;padding:20px 0;">
        <tr><td align="center">
        <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);">
        <tr>
        <td style="background:linear-gradient(135deg,#22c55e,#16a34a);padding:32px 40px;text-align:center;">
        <div style="font-size:48px;margin-bottom:8px;">🌱</div>
        <h1 style="color:#ffffff;font-size:24px;margin:0;font-weight:700;">Good morning, %s!</h1>
        <p style="color:rgba(255,255,255,0.9);margin:8px 0 0;font-size:14px;">Here's your personalized nutrition tip for today</p>
        </td>
        </tr>
        <tr>
        <td style="padding:32px 40px;">
        <div style="background:#f0fdf4;border-left:4px solid #22c55e;padding:16px 20px;border-radius:8px;margin-bottom:24px;">
        <div style="font-size:14px;color:#166534;font-weight:600;margin-bottom:4px;">💬 Today's Tip</div>
        <div style="font-size:16px;color:#14532d;line-height:1.5;">%s</div>
        </div>

        <h2 style="font-size:16px;color:#1e293b;margin:0 0 16px;font-weight:600;">Yesterday's Summary vs Goals</h2>

        <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:20px;">
        <tr>
        <td style="padding-bottom:12px;">
        <div style="display:flex;justify-content:space-between;font-size:13px;color:#64748b;margin-bottom:4px;">
        <span>🔥 Calories</span>
        <span>%d / %d kcal</span>
        </div>
        <div style="background:#e2e8f0;height:10px;border-radius:5px;overflow:hidden;">
        <div style="background:%s;height:10px;width:%.0f%%;border-radius:5px;transition:width 0.3s;"></div>
        </div>
        </td>
        </tr>
        <tr>
        <td style="padding-bottom:12px;">
        <div style="display:flex;justify-content:space-between;font-size:13px;color:#64748b;margin-bottom:4px;">
        <span>🥩 Protein</span>
        <span>%.1f / %d g</span>
        </div>
        <div style="background:#e2e8f0;height:10px;border-radius:5px;overflow:hidden;">
        <div style="background:#3b82f6;height:10px;width:%.0f%%;border-radius:5px;"></div>
        </div>
        </td>
        </tr>
        <tr>
        <td style="padding-bottom:12px;">
        <div style="display:flex;justify-content:space-between;font-size:13px;color:#64748b;margin-bottom:4px;">
        <span>🌾 Carbs</span>
        <span>%.1f / %d g</span>
        </div>
        <div style="background:#e2e8f0;height:10px;border-radius:5px;overflow:hidden;">
        <div style="background:#f59e0b;height:10px;width:%.0f%%;border-radius:5px;"></div>
        </div>
        </td>
        </tr>
        <tr>
        <td style="padding-bottom:8px;">
        <div style="display:flex;justify-content:space-between;font-size:13px;color:#64748b;margin-bottom:4px;">
        <span>🫛 Fat</span>
        <span>%.1f / %d g</span>
        </div>
        <div style="background:#e2e8f0;height:10px;border-radius:5px;overflow:hidden;">
        <div style="background:#ec4899;height:10px;width:%.0f%%;border-radius:5px;"></div>
        </div>
        </td>
        </tr>
        </table>

        <div style="background:#fefce8;border-left:4px solid #eab308;padding:16px 20px;border-radius:8px;margin-bottom:24px;">
        <div style="font-size:14px;color:#854d0e;font-weight:600;margin-bottom:4px;">💡 Try This Today</div>
        <div style="font-size:15px;color:#713f12;line-height:1.5;">%s</div>
        </div>

        <div style="background:#f8fafc;border-radius:8px;padding:16px 20px;text-align:center;margin-bottom:24px;">
        <div style="font-size:14px;color:#64748b;margin-bottom:8px;">🌟 %s</div>
        </div>

        <div style="border-top:1px solid #e2e8f0;padding-top:16px;text-align:center;">
        <p style="font-size:12px;color:#94a3b8;margin:0;">
        You're receiving this because you enabled daily nutrition tips.
        <br>
        <a href="{{UNSUBSCRIBE_LINK}}" style="color:#22c55e;text-decoration:underline;">Unsubscribe</a> from daily tips
        </p>
        </div>
        </td>
        </tr>
        </table>
        </td></tr>
        </table>
        </body>
        </html>
        """.formatted(
                username, tip,
                totalCalories, goalCalories, barColor, calPct,
                totalProtein, goalProtein, protPct,
                totalCarbs, goalCarbs, carbPct,
                totalFat, goalFat, fatPct,
                suggestion, encouragement
        );

        try {
            sendEmail(to, subject, html);
        } catch (IllegalStateException e) {
            LOGGER.error("Daily nutrition tip email was not sent to {}", to, e);
        }
    }

    public void sendCvAnalysisEmail(String to, String candidateName, String jobTitle, Integer score, String skillsFound) {
        String subject = "New CV analysis score for " + jobTitle;
        String skills = skillsFound == null || skillsFound.isBlank() ? "No matching skills found." : skillsFound;
        String displayedScore = score == null ? "N/A" : score + "%";
        String html = "<html><body style='font-family: Arial;'>"
                + "<h2>CV Analysis Complete</h2>"
                + "<p><b>" + candidateName + "</b> has applied for <b>" + jobTitle + "</b>.</p>"
                + "<p>AI match score: <b>" + displayedScore + "</b></p>"
                + "<p>Skills found: " + skills + "</p>"
                + "<p>Log in to review the full details.</p>"
                + "</body></html>";
        try {
            sendEmail(to, subject, html);
        } catch (IllegalStateException e) {
            LOGGER.error("CV analysis email was not sent to {}", to, e);
        }
    }

    public void sendJobPostedEmail(String to, String jobTitle, String companyName, String description) {
        String subject = "New Job Opportunity: " + jobTitle + " at " + companyName;
        String preview = description == null || description.isBlank()
                ? "A new job listing is available now."
                : (description.length() > 200 ? description.substring(0, 200) + "..." : description);
        String html = "<html><body style='font-family: Arial;'>"
                + "<h2>New Job Posted!</h2>"
                + "<p>A new position for <b>" + jobTitle + "</b> at <b>" + companyName + "</b> has just been posted.</p>"
                + "<p><i>" + preview + "</i></p>"
                + "<p>Log in to view more details!</p>"
                + "</body></html>";
        try {
            sendEmail(to, subject, html);
        } catch (IllegalStateException e) {
            LOGGER.error("Job posted email was not sent to {}", to, e);
        }
    }
}
