package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.dto.*;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
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
    private final RoutineRecordRepository routineRecordRepository;
    private final UserRepository userRepository;

    public MainResponse getMainPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String today = getToday();

        List<Routine> routines =
                routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(
                        userId,
                        today
                );

        List<TodayRoutineResponse> todayRoutines = routines.stream()
                .map(routine -> {
                    boolean completed = routineRecordRepository
                            .findByRoutine_IdAndRecordDate(
                                    routine.getId(),
                                    LocalDate.now()
                            )
                            .map(RoutineRecord::isCompleted)
                            .orElse(false);

                    return TodayRoutineResponse.builder()
                            .routineId(routine.getId())
                            .routineName(routine.getTitle())
                            .scheduledTime(routine.getPerformTime())
                            .completed(completed)
                            .build();
                })
                .toList();

        return MainResponse.builder()
                .userName(user.getNickname())
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

    public TodayNotificationListResponse getTodayNotifications(Long userId) {

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

        List<TodayNotificationResponse> response =
                notifications.stream()
                        .map(notification -> TodayNotificationResponse.builder()
                                .notificationId(notification.getId())
                                .routineId(
                                        notification.getRoutine() != null
                                                ? notification.getRoutine().getId()
                                                : null
                                )
                                .content(notification.getContent())
                                .read(notification.isRead())
                                .createdAt(notification.getCreatedAt())
                                .build())
                        .toList();

        boolean hasUnread = notifications.stream()
                .anyMatch(notification -> !notification.isRead());

        return TodayNotificationListResponse.builder()
                .hasUnread(hasUnread)
                .notifications(response)
                .build();
    }

    @Transactional
    public NotificationReadResponse readNotification(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("알림을 찾을 수 없습니다.")
                );

        notification.read();

        return NotificationReadResponse.builder()
                .notificationId(notification.getId())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .build();
    }


}