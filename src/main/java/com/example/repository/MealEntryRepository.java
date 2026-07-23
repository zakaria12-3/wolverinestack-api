package com.example.repository;

import com.example.model.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    List<MealEntry> findByMemberIdOrderByLoggedAtDesc(Long memberId);

    List<MealEntry> findByMemberIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            Long memberId, LocalDateTime start, LocalDateTime end);

    boolean existsByMemberIdAndLoggedAt(Long memberId, LocalDateTime loggedAt);

    void deleteByMemberId(Long memberId);
}
