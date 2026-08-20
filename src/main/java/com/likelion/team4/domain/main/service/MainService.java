package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.dto.*;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final RoutineRepository routineRepository;
    private final NotificationRepository notificationRepository;
    private final RoutineRecordRepository routineRecordRepository;
    private final UserRepository userRepository;
    private final RoutineLogRepository routineLogRepository;
    private final AlternativeMissionRepository alternativeMissionRepository;

    @Transactional
    public MainResponse getMainPage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        String today = getToday();

        List<Routine> routines =
                routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(
                        userId,
                        today
                );

        LocalDate todayDate = LocalDate.now(SEOUL_ZONE);

        List<TodayRoutineResponse> todayRoutines = routines.stream()
                .map(routine -> {

                    RoutineRecord routineRecord =
                            routineRecordRepository
                                    .findByRoutine_IdAndRecordDate(
                                            routine.getId(),
                                            todayDate
                                    )
                                    .orElseGet(() ->
                                            routineRecordRepository.save(
                                                    RoutineRecord.builder()
                                                            .routine(routine)
                                                            .recordDate(todayDate)
                                                            .status(RoutineRecordStatus.PENDING)
                                                            .build()
                                            )
                                    );

                    // 오늘 해당 루틴의 최신 대체 미션 조회
                    AlternativeMission alternativeMission =
                            alternativeMissionRepository
                                    .findTopByAiChat_RoutineLog_Routine_IdAndMissionDateOrderByCreatedAtDesc(
                                            routine.getId(),
                                            todayDate
                                    )
                                    .orElse(null);

                    // 기존 루틴 자체의 완료 여부
                    boolean routineCompleted =
                            routineRecord.getStatus() == RoutineRecordStatus.COMPLETED;

                    String routineStatus =
                            determineRoutineStatus(
                                    routineRecord.getStatus(),
                                    alternativeMission
                            );

                    return TodayRoutineResponse.builder()
                            .routineId(routine.getId())
                            .routineName(routine.getTitle())
                            .scheduledTime(routine.getPerformTime())

                            // 기존 루틴 완료 여부
                            .completed(routineCompleted)

                            // 전체 표시 상태
                            .routineStatus(routineStatus)

                            // 대체 미션 정보
                            .alternativeMissionId(
                                    alternativeMission != null
                                            ? alternativeMission.getId()
                                            : null
                            )
                            .alternativeMissionTitle(
                                    alternativeMission != null
                                            ? alternativeMission.getContent()
                                            : null
                            )
                            .alternativeMissionStatus(
                                    alternativeMission != null
                                            ? alternativeMission.getStatus().name()
                                            : null
                            )
                            .alternativeMissionCompleted(
                                    alternativeMission != null
                                            ? "COMPLETED".equals(alternativeMission.getStatus())
                                            : null
                            )
                            .alternativeMissionCompletedAt(
                                    alternativeMission != null
                                            ? alternativeMission.getCompletedAt()
                                            : null
                            )
                            .build();
                })
                .toList();

        return MainResponse.builder()
                .userName(user.getNickname())
                .todayRoutines(todayRoutines)
                .build();
    }

    private String getToday() {
        return switch (LocalDate.now(SEOUL_ZONE).getDayOfWeek()) {
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

        LocalDate today = LocalDate.now(SEOUL_ZONE);

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
                        new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND)
                );

        notification.read();

        return NotificationReadResponse.builder()
                .notificationId(notification.getId())
                .isRead(notification.isRead())
                .readAt(notification.getReadAt())
                .build();
    }

    private String determineRoutineStatus(
            RoutineRecordStatus recordStatus,
            AlternativeMission alternativeMission
    ) {

        if (recordStatus == RoutineRecordStatus.COMPLETED) {
            return "COMPLETED";
        }

        if (recordStatus == RoutineRecordStatus.INCOMPLETE) {
            return "INCOMPLETE";
        }

        if (alternativeMission == null) {
            return "PENDING";
        }

        return switch (alternativeMission.getStatus()) {
            case COMPLETED -> "ALTERNATIVE_COMPLETED";
            case REJECTED -> "ALTERNATIVE_REJECTED";
            case ACCEPTED, PENDING -> "ALTERNATIVE_IN_PROGRESS";
            default -> "PENDING";
        };
    }
}