package com.likelion.team4.domain.routinelog.service;

import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 오늘자 RoutineLog 조회/생성을 각각 독립된 트랜잭션(REQUIRES_NEW)으로 격리한다.
 * 유니크 제약 위반으로 저장이 실패해도, 호출한 쪽의 트랜잭션/영속성 컨텍스트가
 * 오염되지 않도록 하기 위함 (같은 세션에서 실패 후 재조회하면 세션 자체가 깨져있어 재조회도 실패함).
 */
@Component
@RequiredArgsConstructor
public class RoutineLogProvisioner {

    private static final String INITIAL_STATUS = "미완료";

    private final RoutineLogRepository routineLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<RoutineLog> find(Long routineId, LocalDate logDate) {
        return routineLogRepository.findByRoutine_IdAndLogDate(routineId, logDate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RoutineLog create(Routine routine, LocalDate logDate) {
        return routineLogRepository.save(
                RoutineLog.builder()
                        .routine(routine)
                        .logDate(logDate)
                        .status(INITIAL_STATUS)
                        .reminderSent(false)
                        .build()
        );
    }
}
