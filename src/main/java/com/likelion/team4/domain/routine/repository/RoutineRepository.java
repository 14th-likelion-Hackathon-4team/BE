package com.likelion.team4.domain.routine.repository;
import com.likelion.team4.domain.routine.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findAllByUser_IdAndDeletedAtIsNull(Long userId);
    List<Routine> findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(Long userId, String day);
    Optional<Routine> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);
    List<Routine> findAllByAlarmTrueAndActiveTrueAndDeletedAtIsNull();

    void deleteAllByUser_Id(Long userId);

    @Query("""
    SELECT r
    FROM Routine r
    WHERE r.alarm = true
      AND r.active = true
      AND r.deletedAt IS NULL
      AND r.startDate <= :today
      AND (r.endDate IS NULL OR r.endDate >= :today)
      AND r.repeatDays LIKE CONCAT('%', :todayDay, '%')
      AND r.alarmTime = :now
      AND r.user.routineAlarmOn = true
""")
    List<Routine> findTargetRoutines(
            @Param("today") LocalDate today,
            @Param("todayDay") String todayDay,
            @Param("now") LocalTime now
    );
}
