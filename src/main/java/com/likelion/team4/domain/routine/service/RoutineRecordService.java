package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.report.service.StreakService;
import com.likelion.team4.domain.routine.dto.response.RoutineCompleteResponse;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class RoutineRecordService {

    private final RoutineRepository routineRepository;
    private final RoutineRecordRepository routineRecordRepository;
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

        RoutineRecord record = routineRecordRepository
                .findByRoutine_IdAndRecordDate(routineId, today)
                .orElseGet(() -> RoutineRecord.builder()
                        .routine(routine)
                        .recordDate(today)
                        .status(RoutineRecordStatus.PENDING)
                        .build());

        // ✓ 이미 완료된 루틴 중복 방지
        if (record.getStatus() == RoutineRecordStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ALREADY_COMPLETED_ROUTINE);
        }

        record.complete();
        routineRecordRepository.save(record);

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

        RoutineRecord record = routineRecordRepository
                .findByRoutine_IdAndRecordDate(routineId, date)
                .orElseGet(() -> RoutineRecord.builder()
                        .routine(routine)
                        .recordDate(date)
                        .status(RoutineRecordStatus.PENDING)
                        .build());

        if (record.getStatus() == RoutineRecordStatus.COMPLETED) {
            return;
        }

        record.complete();
        routineRecordRepository.save(record);

        streakService.updateStreak(routine.getUser().getId(), routineId, date);
    }

}