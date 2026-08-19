package com.likelion.team4.domain.routinelog.service;

import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import com.likelion.team4.global.util.DayOfWeekUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

        // 3. 저장 - Spring Data JPA의 save/saveAll은 호출마다 자체 트랜잭션을 가지므로
        //    별도로 @Transactional을 씌우지 않아도 각 건은 트랜잭션 보장 하에 처리됨
        int createdCount = saveRoutineLogs(newRoutineLogs);

        log.info(
                "=== RoutineLog 생성 완료: {}건 (대상 {}건) ===",
                createdCount,
                targetRoutines.size()
        );
    }

    private int saveRoutineLogs(List<RoutineLog> routineLogs) {
        try {
            routineLogRepository.saveAll(routineLogs);
            return routineLogs.size();
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 경로(예: 메인페이지 조회 fallback)에서 먼저 생성한 경우 개별적으로 재시도
            // IDENTITY 전략은 insert 즉시 id가 채워지므로, 롤백 후에도 기존 객체엔 id가 남아있어
            // save()가 merge로 처리될 수 있음 -> id 없는 새 엔티티를 만들어서 재시도
            int createdCount = 0;
            for (RoutineLog routineLog : routineLogs) {
                RoutineLog freshRoutineLog = RoutineLog.builder()
                        .routine(routineLog.getRoutine())
                        .logDate(routineLog.getLogDate())
                        .status(routineLog.getStatus())
                        .reminderSent(routineLog.getReminderSent())
                        .build();
                try {
                    routineLogRepository.save(freshRoutineLog);
                    createdCount++;
                } catch (DataIntegrityViolationException ignored) {
                    log.info(
                            "RoutineLog 중복 생성 시도 무시: routineId={}",
                            routineLog.getRoutine().getId()
                    );
                }
            }
            return createdCount;
        }
    }
}
