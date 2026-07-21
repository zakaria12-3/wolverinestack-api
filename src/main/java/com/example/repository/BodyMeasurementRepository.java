package com.example.repository;

import com.example.model.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {

    List<BodyMeasurement> findByMemberIdOrderByMeasuredAtAsc(Long memberId);

    List<BodyMeasurement> findByMemberIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            Long memberId, LocalDate start, LocalDate end);

    Optional<BodyMeasurement> findTopByMemberIdOrderByMeasuredAtDesc(Long memberId);

    long countByMemberId(Long memberId);
}
