package com.likelion.team4.domain.routinelog.repository;

import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, Long> {
    void deleteAllByRoutine_User_Id(Long userId);

    Optional<RoutineLog> findByRoutine_IdAndLogDate(
            Long routineId,
            LocalDate logDate
    );

    List<RoutineLog> findByRoutine_IdInAndLogDate(
            List<Long> routineIds,
            LocalDate logDate
    );
}