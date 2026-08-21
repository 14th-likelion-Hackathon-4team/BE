package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 오늘자 RoutineRecord 조회/생성을 각각 독립된 트랜잭션(REQUIRES_NEW)으로 격리한다.
 * 유니크 제약 위반으로 저장이 실패해도, 호출한 쪽의 트랜잭션/영속성 컨텍스트가
 * 오염되지 않도록 하기 위함 (같은 세션에서 실패 후 재조회하면 세션 자체가 깨져있어 재조회도 실패함).
 */
@Component
@RequiredArgsConstructor
public class RoutineRecordProvisioner {

    private final RoutineRecordRepository routineRecordRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<RoutineRecord> find(Long routineId, LocalDate recordDate) {
        return routineRecordRepository.findByRoutine_IdAndRecordDate(routineId, recordDate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RoutineRecord create(Routine routine, LocalDate recordDate) {
        return routineRecordRepository.save(
                RoutineRecord.builder()
                        .routine(routine)
                        .recordDate(recordDate)
                        .status(RoutineRecordStatus.PENDING)
                        .build()
        );
    }

    // 완료 처리 + 저장도 별도 트랜잭션으로 격리한다.
    // 낙관적 락(@Version) 충돌로 인한 예외는 호출한 쪽의 트랜잭션과 같은 트랜잭션에서
    // 발생하면 그 트랜잭션 자체가 rollback-only로 표시되어, catch로 잡아도 커밋 시점에
    // UnexpectedRollbackException이 대신 터진다. REQUIRES_NEW로 분리해야 실패가
    // 이 트랜잭션 안에만 갇히고, 호출한 쪽은 정상적으로 계속 진행/커밋될 수 있다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(RoutineRecord record) {
        record.complete();
        routineRecordRepository.save(record);
    }
}
