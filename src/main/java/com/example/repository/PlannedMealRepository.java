package com.example.repository;

import com.example.model.PlannedMeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedMealRepository extends JpaRepository<PlannedMeal, Long> {

    List<PlannedMeal> findByMemberIdAndPlanDateOrderByOrderIndexAsc(Long memberId, LocalDate planDate);

    List<PlannedMeal> findByMemberIdAndPlanDateBetweenOrderByPlanDateAsc(Long memberId,
                                                                          LocalDate start, LocalDate end);

    long countByMemberIdAndPlanDate(Long memberId, LocalDate planDate);
}
