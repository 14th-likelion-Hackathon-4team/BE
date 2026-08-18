package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
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

    private final RoutineRepository routineRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationAiService notificationAiService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void createRoutineNotifications() {

        // 한국 시간 기준
        ZoneId zone = ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone)
                .withSecond(0)
                .withNano(0);

        log.info(
                "=== 알림 스케줄러 실행 === today={}, now={}",
                today,
                now
        );

        String todayDay = switch (today.getDayOfWeek()) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };

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

        List<Long> routineIds = routines.stream()
                .map(Routine::getId)
                .toList();

        if (routineIds.isEmpty()) {
            log.info("=== 알림 대상 루틴 없음 ===");
            return;
        }

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Notification> todayNotifications =
                notificationRepository
                        .findByRoutine_IdInAndCreatedAtBetween(
                                routineIds,
                                start,
                                end
                        );

        Set<Long> notifiedRoutineIds = todayNotifications.stream()
                .map(notification -> notification.getRoutine().getId())
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

            // 오늘 이미 생성된 알림인지 확인
            if (notifiedRoutineIds.contains(routine.getId())) {
                log.info(
                        "루틴 제외 - 오늘 이미 알림 생성됨: routineId={}",
                        routine.getId()
                );
                continue;
            }

            // AI를 이용해 알림 문구 생성
            String content =
                    notificationAiService
                            .generateNotification(routine);

            // 알림 DB 저장
            notificationService.createNotification(
                    routine.getUser(),
                    routine,
                    content
            );

            log.info(
                    "========== 루틴 알림 생성 완료 ==========" +
                            " routineId={}, userId={}, alarmTime={}, content={}",
                    routine.getId(),
                    routine.getUser().getId(),
                    routine.getAlarmTime(),
                    content
            );
        }
    }
}