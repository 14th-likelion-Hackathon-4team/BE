package com.likelion.team4.domain.routinelog.service;

import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RoutineLog 저장(쓰기) 구간만 트랜잭션으로 감싸기 위해 분리한 컴포넌트.
 * RoutineLogScheduler와 별도 빈이어야 @Transactional 프록시가 정상 적용됨(self-invocation 방지).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineLogWriter {

    private final RoutineLogRepository routineLogRepository;

    @Transactional
    public int saveAll(List<RoutineLog> routineLogs) {
        if (routineLogs.isEmpty()) {
            return 0;
        }

        try {
            routineLogRepository.saveAll(routineLogs);
            return routineLogs.size();
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 경로(예: 메인페이지 조회 fallback)에서 먼저 생성한 경우 개별적으로 재시도
            int createdCount = 0;
            for (RoutineLog routineLog : routineLogs) {
                try {
                    routineLogRepository.save(routineLog);
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
