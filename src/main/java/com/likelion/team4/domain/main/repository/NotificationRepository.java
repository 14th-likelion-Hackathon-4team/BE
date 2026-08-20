package com.likelion.team4.domain.main.repository;

import com.likelion.team4.domain.main.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    Optional<Notification> findTopByRoutine_IdOrderByCreatedAtDesc(Long routineId);

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.read = false
        AND n.createdAt <= :time
        """)
    List<Notification> findUnreadNotificationsBefore(
            @Param("time") LocalDateTime time
    );

    @Query("""
        SELECT n
        FROM Notification n
        WHERE n.read = false
        """)
    List<Notification> findAllUnreadNotifications();


    void deleteAllByUser_Id(Long userId);
}