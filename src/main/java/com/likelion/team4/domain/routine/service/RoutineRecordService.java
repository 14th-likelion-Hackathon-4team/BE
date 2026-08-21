package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.report.service.StreakService;
import com.likelion.team4.domain.routine.dto.response.RoutineCompleteResponse;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoutineRecordService {

    private final RoutineRepository routineRepository;
    private final RoutineRecordProvisioner routineRecordProvisioner;
    private final StreakService streakService;
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    public RoutineCompleteResponse completeRoutine(Long routineId) {

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);
        LocalDate today = now.toLocalDate();

        // ✓ 수행 시간 이전이면 완료 불가
        if (routine.getPerformTime() != null
                && now.toLocalTime().isBefore(routine.getPerformTime())) {
            throw new CustomException(ErrorCode.ROUTINE_NOT_YET_TIME);
        }

        RoutineRecord record = findOrCreateRecord(routine, today);

        // ✓ 이미 완료된 루틴 중복 방지
        if (record.getStatus() == RoutineRecordStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ALREADY_COMPLETED_ROUTINE);
        }

        try {
            routineRecordProvisioner.complete(record);
        } catch (ObjectOptimisticLockingFailureException e) {
            // 동시 요청으로 다른 트랜잭션이 먼저 완료 처리한 경우
            throw new CustomException(ErrorCode.ALREADY_COMPLETED_ROUTINE);
        }

        streakService.updateStreak(routine.getUser().getId(), routineId, today);

        return RoutineCompleteResponse.builder()
                .routineId(routineId)
                .completed(true)
                .completedAt(now)
                .build();
    }

    // 대체미션 완료 시 원래 루틴의 RoutineRecord도 함께 완료 처리 (수행 시간 제약 없음)
    public void completeRoutineByAlternativeMission(Long routineId, LocalDate date) {

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        RoutineRecord record = findOrCreateRecord(routine, date);

        if (record.getStatus() == RoutineRecordStatus.COMPLETED) {
            return;
        }

        try {
            routineRecordProvisioner.complete(record);
        } catch (ObjectOptimisticLockingFailureException e) {
            // 동시 요청으로 다른 트랜잭션이 먼저 완료 처리한 경우 - 이미 완료됐으니 조용히 종료
            return;
        }

        streakService.updateStreak(routine.getUser().getId(), routineId, date);
    }

    // 오늘자 RoutineRecord를 조회하고, 없으면 생성한다.
    // 동시 요청으로 둘 다 "없음"으로 판단해 생성을 시도해도, DB 유니크 제약(routine_id, record_date)
    // 위반 시 재조회해서 이미 생성된 레코드를 가져오므로 중복 생성되지 않는다.
    private RoutineRecord findOrCreateRecord(Routine routine, LocalDate date) {
        Optional<RoutineRecord> existing = routineRecordProvisioner.find(routine.getId(), date);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            return routineRecordProvisioner.create(routine, date);
        } catch (DataIntegrityViolationException e) {
            return routineRecordProvisioner.find(routine.getId(), date)
                    .orElseThrow(() -> e);
        }
    }

}