package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineAlarmScheduler {

    private final RoutineRepository routineRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final NotificationAiService notificationAiService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
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
                routineRepository
                        .findAllByAlarmTrueAndActiveTrueAndDeletedAtIsNull();

        log.info(
                "=== 알림 대상 루틴 수: {} ===",
                routines.size()
        );

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

            // 시작일 이전이면 제외
            if (routine.getStartDate() != null
                    && today.isBefore(routine.getStartDate())) {

                log.info(
                        "루틴 제외 - 시작일 이전: routineId={}, startDate={}",
                        routine.getId(),
                        routine.getStartDate()
                );

                continue;
            }

            // 종료일 이후면 제외
            if (routine.getEndDate() != null
                    && today.isAfter(routine.getEndDate())) {

                log.info(
                        "루틴 제외 - 종료일 이후: routineId={}, endDate={}",
                        routine.getId(),
                        routine.getEndDate()
                );

                continue;
            }

            // 반복 요일 확인
            if (routine.getRepeatDays() == null
                    || !routine.getRepeatDays().contains(todayDay)) {

                log.info(
                        "루틴 제외 - 오늘 반복 요일 아님: routineId={}, todayDay={}, repeatDays={}",
                        routine.getId(),
                        todayDay,
                        routine.getRepeatDays()
                );

                continue;
            }

            // 알람 시간이 없으면 제외
            if (routine.getAlarmTime() == null) {

                log.info(
                        "루틴 제외 - 알람 시간 없음: routineId={}",
                        routine.getId()
                );

                continue;
            }

            LocalTime alarmTime =
                    routine.getAlarmTime()
                            .withSecond(0)
                            .withNano(0);

            // 현재 시간과 알람 시간이 같은지 확인
            if (!alarmTime.equals(now)) {

                log.info(
                        "루틴 제외 - 알람 시간 불일치: routineId={}, alarmTime={}, now={}",
                        routine.getId(),
                        alarmTime,
                        now
                );

                continue;
            }

            // 사용자 알림 설정 확인
            if (!routine.getUser().isRoutineAlarmOn()) {

                log.info(
                        "루틴 제외 - 사용자 알림 설정 OFF: routineId={}, userId={}",
                        routine.getId(),
                        routine.getUser().getId()
                );

                continue;
            }

            // 오늘 이미 생성된 알림인지 확인
            LocalDateTime start =
                    today.atStartOfDay();

            LocalDateTime end =
                    today.plusDays(1).atStartOfDay();

            boolean alreadyCreated =
                    notificationRepository
                            .existsByRoutine_IdAndCreatedAtBetween(
                                    routine.getId(),
                                    start,
                                    end
                            );

            if (alreadyCreated) {

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