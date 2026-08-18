package com.likelion.team4.domain.routine.repository;

import com.likelion.team4.domain.routine.entity.RoutineRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineRecordRepository
        extends JpaRepository<RoutineRecord, Long> {

    Optional<RoutineRecord> findByRoutine_IdAndRecordDate(
            Long routineId,
            LocalDate recordDate
    );

    List<RoutineRecord> findAllByRoutine_User_IdAndRecordDateLessThanEqualOrderByRecordDateDesc(
            Long userId,
            LocalDate date
    );

    List<RoutineRecord> findAllByRoutine_User_IdAndRecordDateBetweenOrderByRecordDateDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<RoutineRecord> findAllByRoutine_User_IdAndRecordDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<RoutineRecord> findAllByRoutine_IdInAndRecordDate(
            List<Long> routineIds,
            LocalDate recordDate
    );

    List<RoutineRecord> findAllByRoutine_IdAndRecordDateLessThanEqualOrderByRecordDateDesc(
            Long routineId,
            LocalDate date
    );

    void deleteAllByRoutine_User_Id(Long userId);

}