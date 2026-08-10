package com.likelion.team4.domain.Routine.service;

import com.likelion.team4.domain.Routine.entity.Routine;
import com.likelion.team4.domain.Routine.entity.RoutineRecord;
import com.likelion.team4.domain.Routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.Routine.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class RoutineRecordService {

    private final RoutineRepository routineRepository;
    private final RoutineRecordRepository routineRecordRepository;

    public void completeRoutine(Long routineId) {

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new IllegalArgumentException("루틴을 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        RoutineRecord record = routineRecordRepository
                .findByRoutine_IdAndRecordDate(routineId, today)
                .orElseGet(() -> RoutineRecord.builder()
                        .routine(routine)
                        .recordDate(today)
                        .completed(false)
                        .build());

        record.complete();

        routineRecordRepository.save(record);
    }
}