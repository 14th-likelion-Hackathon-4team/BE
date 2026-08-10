package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.dto.MainResponse;
import com.likelion.team4.domain.main.dto.TodayNotificationResponse;
import com.likelion.team4.domain.main.dto.TodayRoutineResponse;
import com.likelion.team4.domain.Routine.entity.Routine;
import com.likelion.team4.domain.Routine.repository.RoutineRepository;
import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final RoutineRepository routineRepository;
    private final NotificationRepository notificationRepository;

    public MainResponse getMainPage(Long userId) {

        String today = getToday();

        List<Routine> routines =
                routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(
                        userId,
                        today
                );

        List<TodayRoutineResponse> todayRoutines = routines.stream()
                .map(routine -> TodayRoutineResponse.builder()
                        .routineId(routine.getId())
                        .title(routine.getTitle())
                        .performTime(routine.getPerformTime())
                        .completed(false)
                        .build())
                .toList();

        return MainResponse.builder()
                .todayRoutines(todayRoutines)
                .build();
    }

    private String getToday() {
        return switch (LocalDate.now().getDayOfWeek()) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

    public List<TodayNotificationResponse> getTodayNotifications(Long userId) {

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Notification> notifications =
                notificationRepository
                        .findAllByUser_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                                userId,
                                start,
                                end
                        );

        return notifications.stream()
                .map(notification -> TodayNotificationResponse.builder()
                        .notificationId(notification.getId())
                        .content(notification.getContent())
                        .read(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
    }

}