package com.likelion.team4.domain.main.repository;

import com.likelion.team4.domain.main.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Notification> findAllByUser_IdAndContentContainingOrderByCreatedAtDesc(
            Long userId,
            String keyword
    );

    List<Notification> findByRoutine_IdInAndCreatedAtBetween(
            List<Long> routineIds,
            LocalDateTime start,
            LocalDateTime end
    );

    void deleteAllByUser_Id(Long userId);
}