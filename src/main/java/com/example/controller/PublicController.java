package com.example.controller;

import com.example.dto.PlatformRatingSummaryDto;
import com.example.model.Workout;
import com.example.repository.WorkoutRepository;
import com.example.service.PlatformRatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class PublicController {

    private final WorkoutRepository workoutRepository;
    private final PlatformRatingService platformRatingService;
    private final HttpClient httpClient;

    public PublicController(WorkoutRepository workoutRepository, PlatformRatingService platformRatingService) {
        this.workoutRepository = workoutRepository;
        this.platformRatingService = platformRatingService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Workout> workouts = workoutRepository.findVisibleWorkouts();
        PlatformRatingSummaryDto ratings = platformRatingService.getSummary();

        return Map.of(
                "totalWorkouts", workouts.size(),
                "recentWorkouts", workouts.stream().limit(5).map(w -> Map.of(
                        "id", w.getId(),
                        "name", w.getName(),
                        "category", w.getCategory() != null ? w.getCategory() : "",
                        "difficulty", w.getDifficulty() != null ? w.getDifficulty() : "",
                        "muscleGroup", w.getMuscleGroup() != null ? w.getMuscleGroup() : ""
                )).toList(),
                "averageRating", ratings.getAverageRating(),
                "ratingCount", ratings.getRatingCount()
        );
    }

    @GetMapping("/hero-slides")
    public List<Map<String, String>> heroSlides() {
        return List.of(
                Map.of(
                        "role", "MEMBER",
                        "title", "Member Journey",
                        "subtitle", "Track workouts, log meals, and reach your fitness goals",
                        "imageUrl", "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=900&q=80"
                ),
                Map.of(
                        "role", "TRAINER",
                        "title", "Trainer Space",
                        "subtitle", "Create workouts, design plans, and grow your community",
                        "imageUrl", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=900&q=80"
                ),
                Map.of(
                        "role", "COMMUNITY",
                        "title", "Fitness Community",
                        "subtitle", "Share progress, get inspired, and stay accountable together",
                        "imageUrl", "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?auto=format&fit=crop&w=900&q=80"
                )
        );
    }

    @GetMapping("/workouts")
    public List<Workout> workouts() {
        return workoutRepository.findVisibleWorkouts();
    }

    @GetMapping("/workouts/{id}")
    public Workout workout(@PathVariable Long id) {
        return workoutRepository.findById(id)
                .filter(w -> Boolean.TRUE.equals(w.getActive()))
                .filter(w -> w.getModerationStatus() == null || "APPROVED".equalsIgnoreCase(w.getModerationStatus()))
                .orElseThrow(() -> new RuntimeException("Workout not found"));
    }

    // ─── Wger Exercise Library Proxy ─────────────────────────────

    /**
     * Proxies the wger exercise database API, returning cleaned exercise data
     * with images, descriptions, muscle groups, and equipment.
     * Since wger's built-in 'term' search is unreliable, we fetch a batch
     * and filter by name/muscle/equipment server-side.
     * This also avoids CORS issues from calling wger directly from the browser.
     */
    @GetMapping("/exercises/search")
    public Map<String, Object> searchExercises(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "2") int language  // 2 = English
    ) {
        try {
            // Fetch a generous batch — wger's 'term' param doesn't filter properly
            int fetchLimit = Math.min(limit, 60);
            String wgerUrl = "https://wger.de/api/v2/exerciseinfo/?format=json"
                    + "&limit=" + fetchLimit
                    + "&language=" + language;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(wgerUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Map.of("results", List.of(), "error", "wger API returned " + response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> wgerResponse = (Map<String, Object>) new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(response.body(), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawResults = (List<Map<String, Object>>) wgerResponse.getOrDefault("results", List.of());

            // First clean all exercises
            List<Map<String, Object>> cleaned = rawResults.stream()
                    .map(this::cleanExercise)
                    .filter(e -> e.get("name") != null && !((String) e.get("name")).isBlank())
                    .collect(Collectors.toList());

            // Then filter by query server-side (since wger's term param doesn't work)
            List<Map<String, Object>> filtered = cleaned;
            String q = query.trim().toLowerCase();
            if (!q.isBlank()) {
                final String searchTerm = q;
                filtered = cleaned.stream()
                        .filter(ex -> {
                            String name = ((String) ex.getOrDefault("name", "")).toLowerCase();
                            String muscle = ((String) ex.getOrDefault("muscleGroup", "")).toLowerCase();
                            String equip = ((String) ex.getOrDefault("equipment", "")).toLowerCase();
                            String category = ((String) ex.getOrDefault("category", "")).toLowerCase();
                            return name.contains(searchTerm)
                                    || muscle.contains(searchTerm)
                                    || equip.contains(searchTerm)
                                    || category.contains(searchTerm);
                        })
                        .collect(Collectors.toList());
            }

            return Map.of("results", filtered.subList(0, Math.min(filtered.size(), limit)), "count", filtered.size());

        } catch (Exception e) {
            return Map.of("results", List.of(), "error", e.getMessage());
        }
    }

    /** Transform a raw wger exercise object into a clean, frontend-friendly map. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> cleanExercise(Map<String, Object> raw) {
        Map<String, Object> result = new LinkedHashMap<>();

        // ID
        result.put("id", String.valueOf(raw.getOrDefault("id", "")));

        // Name — extract English translation name from the translations array
        String name = "";
        List<Map<String, Object>> translations = (List<Map<String, Object>>) raw.getOrDefault("translations", List.of());
        for (Map<String, Object> t : translations) {
            if (t.get("language") instanceof Number lang && lang.intValue() == 2) { // English = 2
                name = (String) t.getOrDefault("name", "");
                break;
            }
        }
        if (name.isBlank() && !translations.isEmpty()) {
            name = (String) translations.get(0).getOrDefault("name", "");
        }
        result.put("name", name);

        // Description (English)
        String description = "";
        for (Map<String, Object> t : translations) {
            if (t.get("language") instanceof Number lang && lang.intValue() == 2) {
                description = (String) t.getOrDefault("description", "");
                break;
            }
        }
        if (description.isBlank() && !translations.isEmpty()) {
            description = (String) translations.get(0).getOrDefault("description", "");
        }
        result.put("description", stripHtml(description));

        // Muscle group
        List<Map<String, Object>> muscles = (List<Map<String, Object>>) raw.getOrDefault("muscles", List.of());
        List<String> muscleNames = new ArrayList<>();
        for (Map<String, Object> m : muscles) {
            String en = (String) m.getOrDefault("name_en", "");
            if (!en.isBlank()) muscleNames.add(en);
        }
        List<Map<String, Object>> secondaryMuscles = (List<Map<String, Object>>) raw.getOrDefault("muscles_secondary", List.of());
        for (Map<String, Object> m : secondaryMuscles) {
            String en = (String) m.getOrDefault("name_en", "");
            if (!en.isBlank() && !muscleNames.contains(en)) muscleNames.add(en);
        }
        result.put("muscleGroup", muscleNames.isEmpty() ? "General" : String.join(", ", muscleNames));

        // Category
        Map<String, Object> category = (Map<String, Object>) raw.getOrDefault("category", Map.of());
        result.put("category", category.getOrDefault("name", ""));

        // Equipment
        List<Map<String, Object>> equipment = (List<Map<String, Object>>) raw.getOrDefault("equipment", List.of());
        String equipStr = equipment.stream()
                .map(e -> (String) e.getOrDefault("name", ""))
                .filter(s -> !s.isBlank())
                .map(s -> s.replaceAll("\\(.*?\\)", "").trim())
                .collect(Collectors.joining(", "));
        result.put("equipment", equipStr.isBlank() ? "Bodyweight" : equipStr);

        // Image URL — use medium thumbnail for best quality/performance balance
        String imageUrl = "";
        List<Map<String, Object>> images = (List<Map<String, Object>>) raw.getOrDefault("images", List.of());
        if (!images.isEmpty()) {
            Map<String, Object> firstImage = images.get(0);
            Map<String, Object> thumbnails = (Map<String, Object>) firstImage.getOrDefault("thumbnails", Map.of());
            // Prefer medium thumbnail, fall back to full image
            String thumbMedium = (String) thumbnails.getOrDefault("medium", "");
            if (!thumbMedium.isBlank()) {
                imageUrl = thumbMedium;
            } else {
                imageUrl = (String) firstImage.getOrDefault("image", "");
            }
        }

        // If no Wger image, use a placeholder based on muscle group
        if (imageUrl.isBlank()) {
            imageUrl = getPlaceholderImage(muscleNames.isEmpty() ? "General" : muscleNames.get(0));
        }

        result.put("imageUrl", imageUrl);

        return result;
    }

    /** Generate a contextual placeholder image URL based on muscle group. */
    private String getPlaceholderImage(String muscleGroup) {
        String group = muscleGroup.toLowerCase();
        if (group.contains("chest") || group.contains("pectoral")) {
            return "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=400&q=80";
        } else if (group.contains("back") || group.contains("lat") || group.contains("trapezius")) {
            return "https://images.unsplash.com/photo-1603287681836-b174ce5074c2?auto=format&fit=crop&w=400&q=80";
        } else if (group.contains("leg") || group.contains("quad") || group.contains("hamstring") || group.contains("glute")) {
            return "https://images.unsplash.com/photo-1434682881908-b43d0467b798?auto=format&fit=crop&w=400&q=80";
        } else if (group.contains("shoulder") || group.contains("deltoid")) {
            return "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?auto=format&fit=crop&w=400&q=80";
        } else if (group.contains("arm") || group.contains("biceps") || group.contains("triceps")) {
            return "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=400&q=80";
        } else if (group.contains("abs") || group.contains("core") || group.contains("abdominis")) {
            return "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?auto=format&fit=crop&w=400&q=80";
        } else if (group.contains("cardio")) {
            return "https://images.unsplash.com/photo-1538805060514-97d9cc17730c?auto=format&fit=crop&w=400&q=80";
        }
        // Default
        return "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=400&q=80";
    }

    /** Strip HTML tags from a string. */
    private String stripHtml(String html) {
        if (html == null || html.isBlank()) return "";
        return html
                .replaceAll("<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
