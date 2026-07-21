package com.example.service;

import com.example.dto.BodyMeasurementDto;
import com.example.dto.BodyMeasurementProgressDto;
import com.example.dto.BodyMeasurementRequestDto;
import com.example.model.BodyMeasurement;
import com.example.model.MealEntry;
import com.example.model.User;
import com.example.repository.BodyMeasurementRepository;
import com.example.repository.MealEntryRepository;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class BodyMeasurementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BodyMeasurementService.class);

    private final BodyMeasurementRepository measurementRepository;
    private final MealEntryRepository mealEntryRepository;
    private final UserRepository userRepository;

    public BodyMeasurementService(BodyMeasurementRepository measurementRepository,
                                  MealEntryRepository mealEntryRepository,
                                  UserRepository userRepository) {
        this.measurementRepository = measurementRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.userRepository = userRepository;
    }

    /** Log a new body measurement */
    public BodyMeasurementDto logMeasurement(String email, BodyMeasurementRequestDto request) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BodyMeasurement measurement = new BodyMeasurement();
        measurement.setMember(member);
        measurement.setMeasuredAt(request.getMeasuredAt() != null ? request.getMeasuredAt() : LocalDate.now());
        measurement.setWeightKg(request.getWeightKg());
        measurement.setBodyFatPercent(request.getBodyFatPercent());
        measurement.setWaistCm(request.getWaistCm());
        measurement.setHipsCm(request.getHipsCm());
        measurement.setChestCm(request.getChestCm());
        measurement.setLeftArmCm(request.getLeftArmCm());
        measurement.setRightArmCm(request.getRightArmCm());
        measurement.setLeftThighCm(request.getLeftThighCm());
        measurement.setRightThighCm(request.getRightThighCm());
        measurement.setNotes(request.getNotes());

        BodyMeasurement saved = measurementRepository.save(measurement);
        return toDto(saved);
    }

    /** Get all measurements for the member */
    public List<BodyMeasurementDto> getMeasurements(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return measurementRepository.findByMemberIdOrderByMeasuredAtAsc(member.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** Get measurements for a date range */
    public List<BodyMeasurementDto> getMeasurementsForRange(String email, LocalDate start, LocalDate end) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return measurementRepository
                .findByMemberIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(member.getId(), start, end)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /** Get the latest measurement */
    public BodyMeasurementDto getLatestMeasurement(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return measurementRepository.findTopByMemberIdOrderByMeasuredAtDesc(member.getId())
                .map(this::toDto)
                .orElse(null);
    }

    /** Get progress data with optional nutrition overlay for charting */
    public BodyMeasurementProgressDto getProgress(String email, LocalDate start, LocalDate end) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get measurements
        List<BodyMeasurement> measurements = measurementRepository
                .findByMemberIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(member.getId(), start, end);

        BodyMeasurementProgressDto dto = new BodyMeasurementProgressDto();
        dto.setMeasurements(measurements.stream().map(m -> {
            BodyMeasurementProgressDto.MeasurementPoint p = new BodyMeasurementProgressDto.MeasurementPoint();
            p.setDate(m.getMeasuredAt());
            p.setWeightKg(m.getWeightKg());
            p.setBodyFatPercent(m.getBodyFatPercent());
            p.setWaistCm(m.getWaistCm());
            p.setChestCm(m.getChestCm());
            p.setLeftArmCm(m.getLeftArmCm());
            p.setRightArmCm(m.getRightArmCm());
            p.setLeftThighCm(m.getLeftThighCm());
            p.setRightThighCm(m.getRightThighCm());
            return p;
        }).toList());

        // Get nutrition data for the same range (daily aggregates for chart overlay)
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        List<MealEntry> meals = mealEntryRepository
                .findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(member.getId(), startDt, endDt);

        Map<LocalDate, List<MealEntry>> byDate = meals.stream()
                .collect(Collectors.groupingBy(m -> m.getLoggedAt().toLocalDate()));

        List<BodyMeasurementProgressDto.NutritionPoint> dailyCalories = new ArrayList<>();
        List<BodyMeasurementProgressDto.NutritionPoint> dailyProtein = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            List<MealEntry> dayMeals = byDate.getOrDefault(d, List.of());
            int cal = dayMeals.stream().mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0).sum();
            double prot = dayMeals.stream().mapToDouble(m -> m.getProteinGrams() != null ? m.getProteinGrams() : 0).sum();

            BodyMeasurementProgressDto.NutritionPoint calPoint = new BodyMeasurementProgressDto.NutritionPoint();
            calPoint.setDate(d);
            calPoint.setValue(cal);
            dailyCalories.add(calPoint);

            BodyMeasurementProgressDto.NutritionPoint protPoint = new BodyMeasurementProgressDto.NutritionPoint();
            protPoint.setDate(d);
            protPoint.setValue(prot);
            dailyProtein.add(protPoint);
        }

        dto.setDailyCalories(dailyCalories);
        dto.setDailyProtein(dailyProtein);

        return dto;
    }

    /** Update a measurement */
    public BodyMeasurementDto updateMeasurement(Long id, String email, BodyMeasurementRequestDto request) {
        BodyMeasurement measurement = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));
        validateOwnership(measurement, email);

        if (request.getMeasuredAt() != null) measurement.setMeasuredAt(request.getMeasuredAt());
        if (request.getWeightKg() != null) measurement.setWeightKg(request.getWeightKg());
        if (request.getBodyFatPercent() != null) measurement.setBodyFatPercent(request.getBodyFatPercent());
        if (request.getWaistCm() != null) measurement.setWaistCm(request.getWaistCm());
        if (request.getHipsCm() != null) measurement.setHipsCm(request.getHipsCm());
        if (request.getChestCm() != null) measurement.setChestCm(request.getChestCm());
        if (request.getLeftArmCm() != null) measurement.setLeftArmCm(request.getLeftArmCm());
        if (request.getRightArmCm() != null) measurement.setRightArmCm(request.getRightArmCm());
        if (request.getLeftThighCm() != null) measurement.setLeftThighCm(request.getLeftThighCm());
        if (request.getRightThighCm() != null) measurement.setRightThighCm(request.getRightThighCm());
        if (request.getNotes() != null) measurement.setNotes(request.getNotes());

        return toDto(measurementRepository.save(measurement));
    }

    /** Delete a measurement */
    public void deleteMeasurement(Long id, String email) {
        BodyMeasurement measurement = measurementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Measurement not found"));
        validateOwnership(measurement, email);
        measurementRepository.delete(measurement);
    }

    /** Get a quick summary of latest measurements and changes */
    public Map<String, Object> getSummary(String email) {
        User member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<BodyMeasurement> all = measurementRepository
                .findByMemberIdOrderByMeasuredAtAsc(member.getId());

        if (all.isEmpty()) {
            return Map.of("hasData", false, "totalEntries", 0);
        }

        BodyMeasurement latest = all.get(all.size() - 1);
        BodyMeasurement first = all.get(0);

        return Map.of(
                "hasData", true,
                "totalEntries", all.size(),
                "latestDate", latest.getMeasuredAt().toString(),
                "currentWeightKg", latest.getWeightKg(),
                "currentBodyFatPercent", latest.getBodyFatPercent(),
                "currentWaistCm", latest.getWaistCm(),
                "weightChangeKg", first.getWeightKg() != null && latest.getWeightKg() != null
                        ? Math.round((latest.getWeightKg() - first.getWeightKg()) * 10.0) / 10.0 : null,
                "bodyFatChange", first.getBodyFatPercent() != null && latest.getBodyFatPercent() != null
                        ? Math.round((latest.getBodyFatPercent() - first.getBodyFatPercent()) * 10.0) / 10.0 : null,
                "firstDate", first.getMeasuredAt().toString()
        );
    }

    private BodyMeasurementDto toDto(BodyMeasurement m) {
        BodyMeasurementDto dto = new BodyMeasurementDto();
        dto.setId(m.getId());
        dto.setMeasuredAt(m.getMeasuredAt());
        dto.setWeightKg(m.getWeightKg());
        dto.setBodyFatPercent(m.getBodyFatPercent());
        dto.setWaistCm(m.getWaistCm());
        dto.setHipsCm(m.getHipsCm());
        dto.setChestCm(m.getChestCm());
        dto.setLeftArmCm(m.getLeftArmCm());
        dto.setRightArmCm(m.getRightArmCm());
        dto.setLeftThighCm(m.getLeftThighCm());
        dto.setRightThighCm(m.getRightThighCm());
        dto.setNotes(m.getNotes());
        return dto;
    }

    private void validateOwnership(BodyMeasurement measurement, String email) {
        if (!measurement.getMember().getEmail().equals(email)) {
            throw new RuntimeException("Not allowed to modify this measurement");
        }
    }
}
