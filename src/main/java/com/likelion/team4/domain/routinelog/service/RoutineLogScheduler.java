package com.likelion.team4.domain.routinelog.service;

import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import com.likelion.team4.global.util.DayOfWeekUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineLogScheduler {

    private static final String INITIAL_STATUS = "미완료";

    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineLogWriter routineLogWriter;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void createTodayRoutineLogs() {

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        String todayDay = DayOfWeekUtil.toDayCode(today.getDayOfWeek());

        log.info("=== RoutineLog 생성 스케줄러 실행 === today={}", today);

        // 1. 대상 루틴 조회 (트랜잭션 밖, 단순 조회)
        List<Routine> targetRoutines =
                routineRepository.findAllTodayTargetRoutines(today, todayDay);

        if (targetRoutines.isEmpty()) {
            log.info("=== RoutineLog 생성 완료: 0건 (대상 0건) ===");
            return;
        }

        // 2. 이미 생성된 RoutineLog 필터링 (트랜잭션 밖, 조회 + 메모리 연산)
        List<Long> targetRoutineIds = targetRoutines.stream()
                .map(Routine::getId)
                .toList();

        Set<Long> alreadyLoggedRoutineIds = routineLogRepository
                .findByRoutine_IdInAndLogDate(targetRoutineIds, today)
                .stream()
                .map(routineLog -> routineLog.getRoutine().getId())
                .collect(Collectors.toSet());

        List<RoutineLog> newRoutineLogs = targetRoutines.stream()
                .filter(routine -> !alreadyLoggedRoutineIds.contains(routine.getId()))
                .map(routine -> RoutineLog.builder()
                        .routine(routine)
                        .logDate(today)
                        .status(INITIAL_STATUS)
                        .reminderSent(false)
                        .build())
                .toList();

        // 3. 저장 (이 구간만 트랜잭션 - RoutineLogWriter)
        int createdCount = routineLogWriter.saveAll(newRoutineLogs);

        log.info(
                "=== RoutineLog 생성 완료: {}건 (대상 {}건) ===",
                createdCount,
                targetRoutines.size()
        );
    }
}
