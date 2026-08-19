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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineLogScheduler {

    private static final String INITIAL_STATUS = "미완료";

    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void createTodayRoutineLogs() {

        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        String todayDay = DayOfWeekUtil.toDayCode(today.getDayOfWeek());

        log.info("=== RoutineLog 생성 스케줄러 실행 === today={}", today);

        List<Routine> routines =
                routineRepository.findAllTodayTargetRoutines(today, todayDay);

        int createdCount = 0;

        for (Routine routine : routines) {
            boolean alreadyExists = routineLogRepository
                    .findByRoutine_IdAndLogDate(routine.getId(), today)
                    .isPresent();

            if (alreadyExists) {
                continue;
            }

            RoutineLog routineLog = RoutineLog.builder()
                    .routine(routine)
                    .logDate(today)
                    .status(INITIAL_STATUS)
                    .reminderSent(false)
                    .build();

            try {
                routineLogRepository.save(routineLog);
                createdCount++;
            } catch (DataIntegrityViolationException e) {
                // 동시에 다른 경로(예: 메인페이지 조회 fallback)에서 먼저 생성한 경우
                log.info("RoutineLog 중복 생성 시도 무시: routineId={}", routine.getId());
            }
        }

        log.info("=== RoutineLog 생성 완료: {}건 (대상 {}건) ===", createdCount, routines.size());
    }
}
