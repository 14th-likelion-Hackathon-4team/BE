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

@Service
@RequiredArgsConstructor
@Transactional
public class RoutineRecordService {

    private final RoutineRepository routineRepository;
    private final RoutineRecordRepository routineRecordRepository;
    private final StreakService streakService;

    public RoutineCompleteResponse completeRoutine(Long routineId) {

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.ROUTINE_NOT_FOUND)
                );

        LocalDate today = LocalDate.now();
        LocalDateTime completedAt = LocalDateTime.now();

        RoutineRecord record = routineRecordRepository
                .findByRoutine_IdAndRecordDate(routineId, today)
                .orElseGet(() -> RoutineRecord.builder()
                        .routine(routine)
                        .recordDate(today)
                        .status(RoutineRecordStatus.PENDING)
                        .build());

        record.complete();

        routineRecordRepository.save(record);

        // 루틴 완료 후 연속 기록 갱신
        streakService.updateStreak(
                routine.getUser().getId(),
                routineId,
                today
        );

        return RoutineCompleteResponse.builder()
                .routineId(routineId)
                .completed(record.getStatus() == RoutineRecordStatus.COMPLETED)
                .completedAt(completedAt)
                .build();
    }
}