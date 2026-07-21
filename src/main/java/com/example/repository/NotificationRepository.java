package com.example.repository;

import com.example.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop20ByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    long countByRecipientIdAndReadFalse(Long recipientId);
    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);
    boolean existsByRecipientIdAndTypeAndCreatedAtBetween(
            Long recipientId,
            String type,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );
}
