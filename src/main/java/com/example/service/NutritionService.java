package com.example.service;

import com.example.dto.*;
import com.example.model.*;
import com.example.repository.MealEntryRepository;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NutritionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NutritionService.class);

    private final MealEntryRepository mealEntryRepository;
    private final UserRepository userRepository;
    private final FitnessAIService fitnessAIService;

    public NutritionService(MealEntryRepository mealEntryRepository,
                            UserRepository userRepository,
                            FitnessAIService fitnessAIService) {
        this.mealEntryRepository = mealEntryRepository;
        this.userRepository = userRepository;
        this.fitnessAIService = fitnessAIService;
    }

    @Transactional
    public MealLogResponseDto logMeal(LogMealRequestDto request, String email) throws Exception {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MealEntry entry = new MealEntry();
        entry.setMember(member);
        entry.setMealType(MealType.valueOf(MealType.normalize(request.getMealType())));
        entry.setFoodName(request.getFoodName());
        entry.setDescription(request.getDescription());
        entry.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1.0);
        entry.setUnit(request.getUnit() != null ? request.getUnit() : "serving");
        entry.setLoggedAt(LocalDateTime.now());

        // If no calories provided, let AI analyze it
        if (request.getCalories() == null || request.getCalories() <= 0) {
            try {
                Map<String, Object> analysis = fitnessAIService.analyzeMeal(
                        request.getFoodName() + " - " + (request.getDescription() != null ? request.getDescription() : ""),
                        request.getMealType()
                );

                entry.setAiAnalyzed(true);
                if (analysis.get("estimatedCalories") instanceof Number num) {
                    entry.setCalories(num.intValue());
                }
                if (analysis.get("proteinGrams") instanceof Number num) {
                    entry.setProteinGrams(num.doubleValue());
                }
                if (analysis.get("carbsGrams") instanceof Number num) {
                    entry.setCarbsGrams(num.doubleValue());
                }
                if (analysis.get("fatGrams") instanceof Number num) {
                    entry.setFatGrams(num.doubleValue());
                }
                if (analysis.get("fiberGrams") instanceof Number num) {
                    entry.setFiberGrams(num.doubleValue());
                }
                if (analysis.get("confidenceScore") instanceof Number num) {
                    entry.setAiConfidenceScore(num.intValue());
                }
                if (analysis.get("suggestions") instanceof String s) {
                    entry.setAiNotes(s);
                }
            } catch (Exception e) {
                LOGGER.error("AI meal analysis failed", e);
                entry.setCalories(request.getCalories());
            }
        } else {
            entry.setCalories(request.getCalories());
            entry.setProteinGrams(request.getProteinGrams());
            entry.setCarbsGrams(request.getCarbsGrams());
            entry.setFatGrams(request.getFatGrams());
        }

        MealEntry saved = mealEntryRepository.save(entry);
        return new MealLogResponseDto(
                saved.getId(),
                saved.getFoodName(),
                saved.getCalories(),
                saved.getAiAnalyzed() != null && saved.getAiAnalyzed()
        );
    }

    public List<MealEntryDto> getMealsForMember(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mealEntryRepository.findByMemberIdOrderByLoggedAtDesc(member.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<MealEntryDto> getMealsForMemberByDate(String email, LocalDate date) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return mealEntryRepository.findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(
                        member.getId(), start, end)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public MealEntryDto getMealEntry(Long entryId, String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        MealEntry entry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Meal entry not found"));
        if (!Objects.equals(entry.getMember().getId(), member.getId())) {
            throw new RuntimeException("Not allowed to view this meal entry");
        }
        return mapToDto(entry);
    }

    public void deleteMealEntry(Long entryId, String email) {
        MealEntry entry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Meal entry not found"));
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!Objects.equals(entry.getMember().getId(), member.getId())) {
            throw new RuntimeException("Not allowed to delete this meal entry");
        }
        mealEntryRepository.deleteById(entryId);
    }

    @Transactional
    public MealEntryDto updateMealEntry(Long entryId, LogMealRequestDto request, String email) {
        MealEntry entry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Meal entry not found"));
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!Objects.equals(entry.getMember().getId(), member.getId())) {
            throw new RuntimeException("Not allowed to update this meal entry");
        }

        if (request.getMealType() != null) {
            entry.setMealType(MealType.valueOf(MealType.normalize(request.getMealType())));
        }
        if (request.getFoodName() != null) {
            entry.setFoodName(request.getFoodName());
        }
        if (request.getDescription() != null) {
            entry.setDescription(request.getDescription());
        }
        if (request.getQuantity() != null) {
            entry.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            entry.setUnit(request.getUnit());
        }
        if (request.getCalories() != null) {
            entry.setCalories(request.getCalories());
        }
        if (request.getProteinGrams() != null) {
            entry.setProteinGrams(request.getProteinGrams());
        }
        if (request.getCarbsGrams() != null) {
            entry.setCarbsGrams(request.getCarbsGrams());
        }
        if (request.getFatGrams() != null) {
            entry.setFatGrams(request.getFatGrams());
        }

        MealEntry saved = mealEntryRepository.save(entry);
        return mapToDto(saved);
    }

    public Map<String, Object> getDailyNutritionSummary(String email, LocalDate date) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<MealEntry> meals = mealEntryRepository.findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(
                member.getId(), start, end);

        int totalCalories = meals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
        double totalProtein = meals.stream().mapToDouble(m -> m.getProteinGrams() != null ? m.getProteinGrams() : 0).sum();
        double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbsGrams() != null ? m.getCarbsGrams() : 0).sum();
        double totalFat = meals.stream().mapToDouble(m -> m.getFatGrams() != null ? m.getFatGrams() : 0).sum();

        return Map.of(
                "date", date.toString(),
                "totalCalories", totalCalories,
                "totalProtein", Math.round(totalProtein * 10.0) / 10.0,
                "totalCarbs", Math.round(totalCarbs * 10.0) / 10.0,
                "totalFat", Math.round(totalFat * 10.0) / 10.0,
                "mealCount", meals.size(),
                "meals", meals.stream().map(this::mapToDto).toList()
        );
    }

    // ====== TDEE Calculation ======

    /** Calculate TDEE using Mifflin-St Jeor formula with activity multiplier and goal adjustment */
    public TdeeResultDto calculateTdee(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return calculateTdee(user);
    }

    public TdeeResultDto calculateTdee(User user) {
        if (user.getWeightKg() == null || user.getHeightCm() == null || user.getDateOfBirth() == null) {
            throw new RuntimeException("Weight, height, and date of birth are required to calculate TDEE");
        }

        // Calculate age from date of birth using standard Period
        int age = java.time.Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();

        // Mifflin-St Jeor BMR
        // BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age + 5 (male) / -161 (female)
        Gender g = user.getGender() != null ? user.getGender() : Gender.MALE;
        double bmr = switch (g) {
            case FEMALE -> 10 * user.getWeightKg() + 6.25 * user.getHeightCm() - 5 * age - 161;
            default   -> 10 * user.getWeightKg() + 6.25 * user.getHeightCm() - 5 * age + 5;
        };

        // Activity multiplier
        ActivityLevel level = user.getActivityLevel() != null ? user.getActivityLevel() : ActivityLevel.MODERATELY_ACTIVE;
        double activityMultiplier = switch (level) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTREMELY_ACTIVE -> 1.9;
        };

        double tdee = bmr * activityMultiplier;

        // Adjust based on fitness goal
        FitnessGoal goal = user.getFitnessGoal() != null ? user.getFitnessGoal() : FitnessGoal.GENERAL_FITNESS;
        double goalCalories = switch (goal) {
            case LOSE_WEIGHT -> tdee - 500;       // ~0.5kg per week
            case BUILD_MUSCLE -> tdee + 300;      // surplus for muscle gain
            case IMPROVE_ENDURANCE -> tdee;       // maintenance
            case INCREASE_STRENGTH -> tdee + 200; // slight surplus
            case GENERAL_FITNESS -> tdee;         // maintenance
            case FLEXIBILITY -> tdee;             // maintenance
            case SPORTS_PERFORMANCE -> tdee + 300; // performance surplus
        };

        int recommendedCalories = (int) Math.round(goalCalories);

        // Macro split based on goal
        // Protein: 2g per kg of bodyweight for muscle/strength, 1.6g for general
        double proteinPerKg = switch (goal) {
            case BUILD_MUSCLE, INCREASE_STRENGTH -> 2.0;
            case LOSE_WEIGHT -> 2.2;              // higher protein to preserve muscle during deficit
            case SPORTS_PERFORMANCE -> 1.8;
            default -> 1.6;
        };

        int recommendedProtein = (int) Math.round(proteinPerKg * user.getWeightKg());
        int proteinCalories = recommendedProtein * 4;

        // Fat: 25% of total calories (min 0.8g per kg)
        double fatRatio = 0.25;
        int recommendedFat = (int) Math.round((goalCalories * fatRatio) / 9);
        int fatCalories = recommendedFat * 9;

        // Carbs: remaining calories
        int remainingCalories = recommendedCalories - proteinCalories - fatCalories;
        int recommendedCarbs = (int) Math.round((double) remainingCalories / 4);
        if (recommendedCarbs < 0) recommendedCarbs = 0;

        TdeeResultDto result = new TdeeResultDto();
        result.setBmr(Math.round(bmr * 10.0) / 10.0);
        result.setTdee(Math.round(tdee * 10.0) / 10.0);
        result.setActivityLevel(level.name());
        result.setFitnessGoal(goal.name());
        result.setRecommendedCalories(recommendedCalories);
        result.setRecommendedProteinGrams(recommendedProtein);
        result.setRecommendedCarbsGrams(recommendedCarbs);
        result.setRecommendedFatGrams(recommendedFat);
        return result;
    }

    /** Apply TDEE calculation to update user's daily nutrition goals */
    public TdeeResultDto applyTdeeToGoals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        TdeeResultDto tdee = calculateTdee(user);

        user.setDailyCalorieGoal(tdee.getRecommendedCalories());
        user.setDailyProteinGoal(tdee.getRecommendedProteinGrams());
        user.setDailyCarbsGoal(tdee.getRecommendedCarbsGrams());
        user.setDailyFatGoal(tdee.getRecommendedFatGrams());
        userRepository.save(user);

        return tdee;
    }

    // ====== Daily Progress with Goal Percentages ======

    /** Get daily progress with intake vs goals and percentages for progress bars */
    public DailyProgressDto getDailyProgress(String email, LocalDate date) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<MealEntry> meals = mealEntryRepository
                .findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(member.getId(), start, end);

        int totalCalories = meals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
        double totalProtein = meals.stream().mapToDouble(m -> m.getProteinGrams() != null ? m.getProteinGrams() : 0).sum();
        double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbsGrams() != null ? m.getCarbsGrams() : 0).sum();
        double totalFat = meals.stream().mapToDouble(m -> m.getFatGrams() != null ? m.getFatGrams() : 0).sum();

        DailyProgressDto dto = new DailyProgressDto();
        dto.setDate(date);
        dto.setTotalCalories(totalCalories);
        dto.setTotalProtein(Math.round(totalProtein * 10.0) / 10.0);
        dto.setTotalCarbs(Math.round(totalCarbs * 10.0) / 10.0);
        dto.setTotalFat(Math.round(totalFat * 10.0) / 10.0);
        dto.setMealCount(meals.size());
        dto.setMeals(meals.stream().map(this::mapToDto).toList());

        // Set goals
        Integer calGoal = member.getDailyCalorieGoal();
        Integer protGoal = member.getDailyProteinGoal();
        Integer carbGoal = member.getDailyCarbsGoal();
        Integer fatGoal = member.getDailyFatGoal();

        dto.setCalorieGoal(calGoal);
        dto.setProteinGoal(protGoal);
        dto.setCarbsGoal(carbGoal);
        dto.setFatGoal(fatGoal);

        // Calculate percentages (capped at 100% for display, but actual can exceed)
        dto.setCaloriePercent(calGoal != null && calGoal > 0 ? Math.min(100.0, totalCalories * 100.0 / calGoal) : 0);
        dto.setProteinPercent(protGoal != null && protGoal > 0 ? Math.min(100.0, totalProtein * 100.0 / protGoal) : 0);
        dto.setCarbsPercent(carbGoal != null && carbGoal > 0 ? Math.min(100.0, totalCarbs * 100.0 / carbGoal) : 0);
        dto.setFatPercent(fatGoal != null && fatGoal > 0 ? Math.min(100.0, totalFat * 100.0 / fatGoal) : 0);

        // Calculate remaining (negative means over goal)
        dto.setCaloriesRemaining(calGoal != null ? calGoal - totalCalories : 0);
        dto.setProteinRemaining(protGoal != null ? Math.round((protGoal - totalProtein) * 10.0) / 10.0 : 0);
        dto.setCarbsRemaining(carbGoal != null ? Math.round((carbGoal - totalCarbs) * 10.0) / 10.0 : 0);
        dto.setFatRemaining(fatGoal != null ? Math.round((fatGoal - totalFat) * 10.0) / 10.0 : 0);

        return dto;
    }

    // ====== Weekly Nutrition Report ======

    /** Get a 7-day nutrition report with averages, best day, and macro split */
    public WeeklyReportDto getWeeklyReport(String email) {
        return getWeeklyReport(email, LocalDate.now());
    }

    public WeeklyReportDto getWeeklyReport(String email, LocalDate endDate) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate startDate = endDate.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<MealEntry> meals = mealEntryRepository
                .findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(member.getId(), start, end);

        // Group by date
        Map<LocalDate, List<MealEntry>> byDate = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getLoggedAt().toLocalDate()));

        int daysTracked = byDate.size();
        if (daysTracked == 0) {
            WeeklyReportDto empty = new WeeklyReportDto();
            empty.setStartDate(startDate);
            empty.setEndDate(endDate);
            empty.setDaysTracked(0);
            empty.setDailySummaries(List.of());
            return empty;
        }

        // Build daily summaries
        List<WeeklyReportDto.DailySummary> dailySummaries = new ArrayList<>();
        int weekTotalCalories = 0;
        double weekTotalProtein = 0;
        double weekTotalCarbs = 0;
        double weekTotalFat = 0;
        int bestCalories = 0;
        String bestCalorieDate = "";

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            List<MealEntry> dayMeals = byDate.getOrDefault(d, List.of());
            int dayCals = dayMeals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
            double dayProt = dayMeals.stream().mapToDouble(m -> m.getProteinGrams() != null ? m.getProteinGrams() : 0).sum();
            double dayCarbs = dayMeals.stream().mapToDouble(m -> m.getCarbsGrams() != null ? m.getCarbsGrams() : 0).sum();
            double dayFat = dayMeals.stream().mapToDouble(m -> m.getFatGrams() != null ? m.getFatGrams() : 0).sum();

            weekTotalCalories += dayCals;
            weekTotalProtein += dayProt;
            weekTotalCarbs += dayCarbs;
            weekTotalFat += dayFat;

            if (dayCals > bestCalories) {
                bestCalories = dayCals;
                bestCalorieDate = d.toString();
            }

            double calPct = 0;
            if (member.getDailyCalorieGoal() != null && member.getDailyCalorieGoal() > 0) {
                calPct = Math.min(100.0, dayCals * 100.0 / member.getDailyCalorieGoal());
            }

            WeeklyReportDto.DailySummary summary = new WeeklyReportDto.DailySummary();
            summary.setDate(d);
            summary.setCalories(dayCals);
            summary.setCaloriePercent(Math.round(calPct * 10.0) / 10.0);
            summary.setProtein(Math.round(dayProt * 10.0) / 10.0);
            summary.setCarbs(Math.round(dayCarbs * 10.0) / 10.0);
            summary.setFat(Math.round(dayFat * 10.0) / 10.0);
            summary.setMealCount(dayMeals.size());
            dailySummaries.add(summary);
        }

        // Averages
        double avgCal = daysTracked > 0 ? (double) weekTotalCalories / daysTracked : 0;
        double avgProt = daysTracked > 0 ? weekTotalProtein / daysTracked : 0;
        double avgCarbs = daysTracked > 0 ? weekTotalCarbs / daysTracked : 0;
        double avgFat = daysTracked > 0 ? weekTotalFat / daysTracked : 0;

        // Macro split (percentage of total calories from each)
        double proteinCals = weekTotalProtein * 4;
        double carbsCals = weekTotalCarbs * 4;
        double fatCals = weekTotalFat * 9;
        double totalMacroCals = proteinCals + carbsCals + fatCals;

        double protPct = totalMacroCals > 0 ? Math.round(proteinCals * 100.0 / totalMacroCals * 10.0) / 10.0 : 0;
        double carbsPct = totalMacroCals > 0 ? Math.round(carbsCals * 100.0 / totalMacroCals * 10.0) / 10.0 : 0;
        double fatPct = totalMacroCals > 0 ? Math.round(fatCals * 100.0 / totalMacroCals * 10.0) / 10.0 : 0;

        Integer calGoal = member.getDailyCalorieGoal();
        Integer protGoal = member.getDailyProteinGoal();
        Integer carbGoal = member.getDailyCarbsGoal();
        Integer fatGoal = member.getDailyFatGoal();

        WeeklyReportDto report = new WeeklyReportDto();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setDaysTracked(daysTracked);
        report.setTotalCalories(weekTotalCalories);
        report.setTotalProtein(Math.round(weekTotalProtein * 10.0) / 10.0);
        report.setTotalCarbs(Math.round(weekTotalCarbs * 10.0) / 10.0);
        report.setTotalFat(Math.round(weekTotalFat * 10.0) / 10.0);
        report.setAvgDailyCalories(Math.round(avgCal * 10.0) / 10.0);
        report.setAvgDailyProtein(Math.round(avgProt * 10.0) / 10.0);
        report.setAvgDailyCarbs(Math.round(avgCarbs * 10.0) / 10.0);
        report.setAvgDailyFat(Math.round(avgFat * 10.0) / 10.0);
        report.setAvgCalorieGoal(calGoal);
        report.setAvgProteinGoal(protGoal);
        report.setAvgCarbsGoal(carbGoal);
        report.setAvgFatGoal(fatGoal);
        report.setAvgCaloriePercent(calGoal != null && calGoal > 0 ? Math.round(avgCal * 100.0 / calGoal * 10.0) / 10.0 : 0);
        report.setAvgProteinPercent(protGoal != null && protGoal > 0 ? Math.round(avgProt * 100.0 / protGoal * 10.0) / 10.0 : 0);
        report.setAvgCarbsPercent(carbGoal != null && carbGoal > 0 ? Math.round(avgCarbs * 100.0 / carbGoal * 10.0) / 10.0 : 0);
        report.setAvgFatPercent(fatGoal != null && fatGoal > 0 ? Math.round(avgFat * 100.0 / fatGoal * 10.0) / 10.0 : 0);
        report.setBestCalorieDay(bestCalories);
        report.setBestCalorieDayDate(bestCalorieDate);
        report.setProteinCaloriePercent(protPct);
        report.setCarbsCaloriePercent(carbsPct);
        report.setFatCaloriePercent(fatPct);
        report.setDailySummaries(dailySummaries);

        return report;
    }

    private MealEntryDto mapToDto(MealEntry entry) {
        MealEntryDto dto = new MealEntryDto();
        dto.setId(entry.getId());
        dto.setMemberName(entry.getMember().getRealUsername());
        dto.setMealType(entry.getMealType() != null ? entry.getMealType().name() : null);
        dto.setFoodName(entry.getFoodName());
        dto.setDescription(entry.getDescription());
        dto.setQuantity(entry.getQuantity());
        dto.setUnit(entry.getUnit());
        dto.setCalories(entry.getCalories());
        dto.setProteinGrams(entry.getProteinGrams());
        dto.setCarbsGrams(entry.getCarbsGrams());
        dto.setFatGrams(entry.getFatGrams());
        dto.setFiberGrams(entry.getFiberGrams());
        dto.setAiAnalyzed(entry.getAiAnalyzed());
        dto.setAiConfidenceScore(entry.getAiConfidenceScore());
        dto.setAiNotes(entry.getAiNotes());
        dto.setLoggedAt(entry.getLoggedAt());
        return dto;
    }
}
