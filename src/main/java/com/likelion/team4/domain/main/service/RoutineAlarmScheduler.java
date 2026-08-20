package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.global.util.DayOfWeekUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineAlarmScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final RoutineRepository routineRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationAiService notificationAiService;

    // 최초 알림 생성
    // 테스트를 위해 현재는 매분 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void createRoutineNotifications() {

        LocalDate today = LocalDate.now(SEOUL_ZONE);

        LocalTime now = LocalTime.now(SEOUL_ZONE)
                .withSecond(0)
                .withNano(0);

        log.info(
                "=== 최초 알림 스케줄러 실행 === today={}, now={}",
                today,
                now
        );

        String todayDay =
                DayOfWeekUtil.toDayCode(today.getDayOfWeek());

        List<Routine> routines =
                routineRepository.findTargetRoutines(
                        today,
                        todayDay,
                        now
                );

        log.info(
                "=== 알림 대상 루틴 수: {} ===",
                routines.size()
        );

        if (routines.isEmpty()) {
            log.info("=== 알림 대상 루틴 없음 ===");
            return;
        }

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Long> routineIds = routines.stream()
                .map(Routine::getId)
                .toList();

        // 오늘 이미 생성된 알림 확인
        List<Notification> todayNotifications =
                notificationRepository
                        .findByRoutine_IdInAndCreatedAtBetween(
                                routineIds,
                                start,
                                end
                        );

        Set<Long> notifiedRoutineIds =
                todayNotifications.stream()
                        .map(notification ->
                                notification.getRoutine().getId()
                        )
                        .collect(Collectors.toSet());

        for (Routine routine : routines) {

            log.info(
                    "루틴 확인 - routineId={}, title={}, alarm={}, active={}, alarmTime={}, repeatDays={}",
                    routine.getId(),
                    routine.getTitle(),
                    routine.isAlarm(),
                    routine.isActive(),
                    routine.getAlarmTime(),
                    routine.getRepeatDays()
            );

            // 오늘 이미 최초 알림이 생성된 경우
            // 재알림은 별도 스케줄러에서 처리
            if (notifiedRoutineIds.contains(routine.getId())) {
                log.info(
                        "루틴 제외 - 오늘 이미 최초 알림 생성됨: routineId={}",
                        routine.getId()
                );
                continue;
            }

            String content =
                    notificationAiService
                            .generateNotification(routine);

            notificationService.createNotification(
                    routine.getUser(),
                    routine,
                    content
            );

            log.info(
                    "========== 최초 알림 생성 완료 ==========" +
                            " routineId={}, userId={}, alarmTime={}, content={}",
                    routine.getId(),
                    routine.getUser().getId(),
                    routine.getAlarmTime(),
                    content
            );
        }
    }
}