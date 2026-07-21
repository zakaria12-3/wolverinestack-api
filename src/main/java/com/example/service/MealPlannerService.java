package com.example.service;

import com.example.dto.*;
import com.example.model.*;
import com.example.repository.MealEntryRepository;
import com.example.repository.PlannedMealRepository;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class MealPlannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MealPlannerService.class);

    private final PlannedMealRepository plannedMealRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserRepository userRepository;
    private final FitnessAIService fitnessAIService;

    public MealPlannerService(PlannedMealRepository plannedMealRepository,
                              MealEntryRepository mealEntryRepository,
                              UserRepository userRepository,
                              FitnessAIService fitnessAIService) {
        this.plannedMealRepository = plannedMealRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.userRepository = userRepository;
        this.fitnessAIService = fitnessAIService;
    }

    /** Add a meal to the plan for a specific date */
    public PlannedMealDto addMealToPlan(String email, MealPlannerRequestDto request) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getPlanDate() == null) {
            throw new RuntimeException("Plan date is required");
        }

        PlannedMeal planned = new PlannedMeal();
        planned.setMember(member);
        planned.setPlanDate(request.getPlanDate());
        planned.setMealType(MealType.valueOf(MealType.normalize(request.getMealType())));
        planned.setFoodName(request.getFoodName());
        planned.setDescription(request.getDescription());
        planned.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1.0);
        planned.setUnit(request.getUnit() != null ? request.getUnit() : "serving");
        planned.setEstimatedCalories(request.getEstimatedCalories());
        planned.setEstimatedProtein(request.getEstimatedProtein());
        planned.setEstimatedCarbs(request.getEstimatedCarbs());
        planned.setEstimatedFat(request.getEstimatedFat());
        planned.setOrderIndex(request.getOrderIndex());
        planned.setConsumed(false);

        PlannedMeal saved = plannedMealRepository.save(planned);
        return toDto(saved);
    }

    /** Get all planned meals for a specific date */
    public List<PlannedMealDto> getMealPlan(String email, LocalDate date) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return plannedMealRepository
                .findByMemberIdAndPlanDateOrderByOrderIndexAsc(member.getId(), date)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** Get the full meal plan projection with goals and remaining macros */
    public MealPlanProjectionDto getProjection(String email, LocalDate date) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PlannedMeal> planned = plannedMealRepository
                .findByMemberIdAndPlanDateOrderByOrderIndexAsc(member.getId(), date);

        // Sum up planned macros
        int projCal = planned.stream().mapToInt(m -> m.getEstimatedCalories() != null ? m.getEstimatedCalories() : 0).sum();
        double projProt = planned.stream().mapToDouble(m -> m.getEstimatedProtein() != null ? m.getEstimatedProtein() : 0).sum();
        double projCarbs = planned.stream().mapToDouble(m -> m.getEstimatedCarbs() != null ? m.getEstimatedCarbs() : 0).sum();
        double projFat = planned.stream().mapToDouble(m -> m.getEstimatedFat() != null ? m.getEstimatedFat() : 0).sum();

        Integer calGoal = member.getDailyCalorieGoal();
        Integer protGoal = member.getDailyProteinGoal();
        Integer carbGoal = member.getDailyCarbsGoal();
        Integer fatGoal = member.getDailyFatGoal();

        // Remaining after planned
        int remCal = calGoal != null ? Math.max(0, calGoal - projCal) : 0;
        double remProt = protGoal != null ? Math.max(0, protGoal - projProt) : 0;
        double remCarbs = carbGoal != null ? Math.max(0, carbGoal - projCarbs) : 0;
        double remFat = fatGoal != null ? Math.max(0, fatGoal - projFat) : 0;

        MealPlanProjectionDto dto = new MealPlanProjectionDto();
        dto.setDate(date);
        dto.setPlannedMeals(planned.stream().map(this::toDto).toList());
        dto.setMealCount(planned.size());
        dto.setProjectedCalories(projCal);
        dto.setProjectedProtein(Math.round(projProt * 10.0) / 10.0);
        dto.setProjectedCarbs(Math.round(projCarbs * 10.0) / 10.0);
        dto.setProjectedFat(Math.round(projFat * 10.0) / 10.0);
        dto.setCalorieGoal(calGoal);
        dto.setProteinGoal(protGoal);
        dto.setCarbsGoal(carbGoal);
        dto.setFatGoal(fatGoal);
        dto.setRemainingCalories(remCal);
        dto.setRemainingProtein(Math.round(remProt * 10.0) / 10.0);
        dto.setRemainingCarbs(Math.round(remCarbs * 10.0) / 10.0);
        dto.setRemainingFat(Math.round(remFat * 10.0) / 10.0);
        dto.setCaloriePercent(calGoal != null && calGoal > 0 ? Math.min(100.0, projCal * 100.0 / calGoal) : 0);
        dto.setProteinPercent(protGoal != null && protGoal > 0 ? Math.min(100.0, projProt * 100.0 / protGoal) : 0);
        dto.setCarbsPercent(carbGoal != null && carbGoal > 0 ? Math.min(100.0, projCarbs * 100.0 / carbGoal) : 0);
        dto.setFatPercent(fatGoal != null && fatGoal > 0 ? Math.min(100.0, projFat * 100.0 / fatGoal) : 0);

        // Get AI suggestions if there are remaining macros to fill
        if (remCal > 100) {
            dto.setAiSuggestions(getAISuggestions(remCal, remProt, remCarbs, remFat));
        } else {
            dto.setAiSuggestions(List.of());
        }

        return dto;
    }

    /** Update a planned meal */
    public PlannedMealDto updatePlannedMeal(Long mealId, String email, MealPlannerRequestDto request) {
        PlannedMeal planned = plannedMealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Planned meal not found"));
        validateOwnership(planned, email);

        if (request.getPlanDate() != null) planned.setPlanDate(request.getPlanDate());
        if (request.getMealType() != null)
            planned.setMealType(MealType.valueOf(MealType.normalize(request.getMealType())));
        if (request.getFoodName() != null) planned.setFoodName(request.getFoodName());
        if (request.getDescription() != null) planned.setDescription(request.getDescription());
        if (request.getQuantity() != null) planned.setQuantity(request.getQuantity());
        if (request.getUnit() != null) planned.setUnit(request.getUnit());
        if (request.getEstimatedCalories() != null) planned.setEstimatedCalories(request.getEstimatedCalories());
        if (request.getEstimatedProtein() != null) planned.setEstimatedProtein(request.getEstimatedProtein());
        if (request.getEstimatedCarbs() != null) planned.setEstimatedCarbs(request.getEstimatedCarbs());
        if (request.getEstimatedFat() != null) planned.setEstimatedFat(request.getEstimatedFat());
        if (request.getOrderIndex() != null) planned.setOrderIndex(request.getOrderIndex());

        return toDto(plannedMealRepository.save(planned));
    }

    /** Delete a planned meal */
    public void deletePlannedMeal(Long mealId, String email) {
        PlannedMeal planned = plannedMealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Planned meal not found"));
        validateOwnership(planned, email);
        plannedMealRepository.delete(planned);
    }

    /** Mark a planned meal as consumed, which logs it as an actual MealEntry */
    public MealLogResponseDto markAsConsumed(Long mealId, String email) {
        PlannedMeal planned = plannedMealRepository.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Planned meal not found"));
        validateOwnership(planned, email);

        planned.setConsumed(true);
        plannedMealRepository.save(planned);

        // Create actual meal entry from the planned meal
        MealEntry entry = new MealEntry();
        entry.setMember(planned.getMember());
        entry.setMealType(planned.getMealType());
        entry.setFoodName(planned.getFoodName());
        entry.setDescription(planned.getDescription());
        entry.setQuantity(planned.getQuantity() != null ? planned.getQuantity() : 1.0);
        entry.setUnit(planned.getUnit() != null ? planned.getUnit() : "serving");
        entry.setCalories(planned.getEstimatedCalories());
        entry.setProteinGrams(planned.getEstimatedProtein());
        entry.setCarbsGrams(planned.getEstimatedCarbs());
        entry.setFatGrams(planned.getEstimatedFat());
        entry.setLoggedAt(LocalDateTime.now());

        MealEntry saved = mealEntryRepository.save(entry);
        return new MealLogResponseDto(
                saved.getId(),
                saved.getFoodName(),
                saved.getCalories(),
                false
        );
    }

    /** Get AI suggestions for filling remaining macros */
    public List<MealPlanProjectionDto.AISuggestion> getAISuggestions(
            int remainingCalories, double remainingProtein,
            double remainingCarbs, double remainingFat) {

        try {
            Map<String, Object> aiResult = fitnessAIService.suggestMealsForRemainingMacros(
                    remainingCalories, remainingProtein, remainingCarbs, remainingFat, null);

            List<MealPlanProjectionDto.AISuggestion> suggestions = new ArrayList<>();
            if (aiResult.get("suggestions") instanceof List<?> rawSuggestions) {
                for (Object raw : rawSuggestions) {
                    if (raw instanceof Map<?, ?> rawMap) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> s = (Map<String, Object>) rawMap;
                        MealPlanProjectionDto.AISuggestion sug = new MealPlanProjectionDto.AISuggestion();
                        sug.setSuggestion((String) s.getOrDefault("suggestion", ""));
                        sug.setMealType((String) s.getOrDefault("mealType", "SNACK"));
                        sug.setEstimatedCalories(s.get("estimatedCalories") instanceof Number n ? n.intValue() : 0);
                        sug.setEstimatedProtein(s.get("estimatedProtein") instanceof Number n ? n.doubleValue() : 0);
                        sug.setEstimatedCarbs(s.get("estimatedCarbs") instanceof Number n ? n.doubleValue() : 0);
                        sug.setEstimatedFat(s.get("estimatedFat") instanceof Number n ? n.doubleValue() : 0);
                        sug.setReasoning((String) s.getOrDefault("reasoning", ""));
                        suggestions.add(sug);
                    }
                }
            }
            return suggestions;
        } catch (Exception e) {
            LOGGER.warn("AI meal suggestions failed", e);
            return List.of();
        }
    }

    private PlannedMealDto toDto(PlannedMeal pm) {
        PlannedMealDto dto = new PlannedMealDto();
        dto.setId(pm.getId());
        dto.setPlanDate(pm.getPlanDate());
        dto.setMealType(pm.getMealType() != null ? pm.getMealType().name() : null);
        dto.setFoodName(pm.getFoodName());
        dto.setDescription(pm.getDescription());
        dto.setQuantity(pm.getQuantity());
        dto.setUnit(pm.getUnit());
        dto.setEstimatedCalories(pm.getEstimatedCalories());
        dto.setEstimatedProtein(pm.getEstimatedProtein());
        dto.setEstimatedCarbs(pm.getEstimatedCarbs());
        dto.setEstimatedFat(pm.getEstimatedFat());
        dto.setConsumed(pm.getConsumed());
        dto.setOrderIndex(pm.getOrderIndex());
        return dto;
    }

    private void validateOwnership(PlannedMeal planned, String email) {
        if (!planned.getMember().getEmail().equals(email)) {
            throw new RuntimeException("Not allowed to modify this planned meal");
        }
    }
}
