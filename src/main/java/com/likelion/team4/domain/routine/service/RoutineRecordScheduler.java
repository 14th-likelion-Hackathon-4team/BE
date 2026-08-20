package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoutineRecordScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final RoutineRecordRepository routineRecordRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void markIncompleteRoutineRecords() {

        LocalDate yesterday =
                LocalDate.now(SEOUL_ZONE).minusDays(1);

        List<RoutineRecord> records =
                routineRecordRepository.findAllByRecordDateAndStatus(
                        yesterday,
                        RoutineRecordStatus.PENDING
                );

        records.forEach(RoutineRecord::markIncomplete);
    }
}