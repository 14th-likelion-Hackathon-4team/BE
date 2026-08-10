package com.likelion.team4.domain.Routine.repository;

import com.likelion.team4.domain.Routine.entity.RoutineRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RoutineRecordRepository
        extends JpaRepository<RoutineRecord, Long> {

    Optional<RoutineRecord> findByRoutine_IdAndRecordDate(
            Long routineId,
            LocalDate recordDate
    );
}