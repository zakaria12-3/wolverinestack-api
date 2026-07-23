package com.example.service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class AIService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIService.class);

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_MS = 1000; // 1 second initial backoff
    private static final long MAX_RETRY_MS = 30_000; // 30 second cap

    @Value("${GROQ_API_KEY:}")
    private String apiKey;

    @Value("${GROQ_MODEL:llama-3.3-70b-versatile}")
    private String defaultModel;

    @Value("${GROQ_VISION_MODEL:qwen/qwen3.6-27b}")
    private String visionModel;

    public String askAI(String prompt) {
        return askAI(prompt, false);
    }

    public String askAI(String prompt, boolean requireJson) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        return sendChatRequest(defaultModel, List.of(message), 1024, requireJson);
    }

    public String askVision(String prompt, String imageDataUrl, boolean requireJson) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", imageDataUrl);

        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrl);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", List.of(textPart, imagePart));

        return sendChatRequest(visionModel, List.of(message), 1200, requireJson);
    }

    private String sendChatRequest(String model, List<Map<String, Object>> messages, int maxTokens, boolean requireJson) {
        if (!hasConfiguredApiKey()) {
            return "AI error: GROQ_API_KEY is not configured";
        }

        ObjectMapper mapper = new ObjectMapper();
        int attempt = 0;
        long waitMs = BASE_RETRY_MS;

        while (attempt < MAX_RETRIES) {
            attempt++;
            HttpURLConnection conn = null;
            try {
                URL url = URI.create(GROQ_URL).toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15_000);
                conn.setReadTimeout(60_000);

                Map<String, Object> body = new HashMap<>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("max_tokens", maxTokens);
                if (requireJson) {
                    body.put("response_format", Map.of("type", "json_object"));
                }

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(mapper.writeValueAsBytes(body));
                }

                int statusCode = conn.getResponseCode();
                StringBuilder responseBody = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                statusCode < 300
                                        ? conn.getInputStream()
                                        : conn.getErrorStream()
                        )
                )) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBody.append(line);
                    }
                }

                if (statusCode < 300) {
                    Map<String, Object> json = mapper.readValue(responseBody.toString(), new TypeReference<>() {});
                    List<?> choices = (List<?>) json.get("choices");
                    if (choices == null || choices.isEmpty()) {
                        return "AI error: empty choices in response";
                    }
                    Map<?, ?> first = (Map<?, ?>) choices.get(0);
                    Map<?, ?> msg = (Map<?, ?>) first.get("message");
                    return (String) msg.get("content");
                }

                if (statusCode == 429) {
                    long retryAfter = parseRetryAfterFromError(responseBody.toString(), waitMs);
                    if (attempt >= MAX_RETRIES) {
                        LOGGER.warn("Groq rate limit exceeded after {} attempts. Giving up.", attempt);
                        return "AI error: Rate limit exceeded after retries. Please try again later.";
                    }
                    LOGGER.info("Groq rate limited (attempt {}/{}). Waiting {}ms before retry.",
                            attempt, MAX_RETRIES, retryAfter);
                    Thread.sleep(retryAfter);
                    waitMs = Math.min(waitMs * 2 + (long) (Math.random() * 1000), MAX_RETRY_MS);
                    continue;
                }

                LOGGER.error("Groq API error {}: {}", statusCode, responseBody);
                return "AI error: " + responseBody;

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return "AI error: Request interrupted";
            } catch (IOException e) {
                if (attempt >= MAX_RETRIES) {
                    LOGGER.error("AI request failed after {} retries", MAX_RETRIES, e);
                    return "AI error: " + e.getMessage() + " (after " + MAX_RETRIES + " retries)";
                }
                LOGGER.warn("AI request transient error (attempt {}/{}): {}. Retrying in {}ms.",
                        attempt, MAX_RETRIES, e.getMessage(), waitMs);
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "AI error: Request interrupted";
                }
                waitMs = Math.min(waitMs * 2 + (long) (Math.random() * 1000), MAX_RETRY_MS);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        return "AI error: Maximum retries exceeded";
    }

    /**
     * Parse retry-after duration from Groq's rate limit error response.
     * Looks for "Please try again in Xms" in the error message.
     * Falls back to the provided defaultMs if parsing fails.
     */
    private long parseRetryAfterFromError(String errorBody, long defaultMs) {
        try {
            // Try to find "Please try again in <number>ms" pattern
            int idx = errorBody.indexOf("Please try again in ");
            if (idx >= 0) {
                int start = idx + "Please try again in ".length();
                int end = errorBody.indexOf("ms", start);
                if (end > start) {
                    String numStr = errorBody.substring(start, end).trim();
                    long ms = Long.parseLong(numStr);
                    // Add 500ms safety buffer + small jitter
                    return ms + 500 + (long)(Math.random() * 500);
                }
            }
            // Try to find Retry-After header equivalent in body
            if (errorBody.contains("rate_limit_exceeded")) {
                return defaultMs;
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse retry-after from error: {}", e.getMessage());
        }
        return defaultMs;
    }

    public boolean hasConfiguredApiKey() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equalsIgnoreCase("changeme");
    }
}
