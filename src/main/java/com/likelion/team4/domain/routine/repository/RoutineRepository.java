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

    @Query("""
        SELECT r
        FROM Routine r
        WHERE r.user.id = :userId
          AND r.deletedAt IS NULL
          AND r.active = true
          AND (r.startDate IS NULL OR r.startDate <= :date)
          AND (r.endDate IS NULL OR r.endDate >= :date)
          AND r.repeatDays LIKE CONCAT('%', :day, '%')
        """)
    List<Routine> findTargetRoutines(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("day") String day
    );

    @Query("""
    SELECT r
    FROM Routine r
    WHERE r.user.id = :userId
      AND r.deletedAt IS NULL
      AND r.active = true
      AND (r.startDate IS NULL OR r.startDate <= :endDate)
      AND (r.endDate IS NULL OR r.endDate >= :startDate)
      AND (
            r.repeatDays LIKE '%MON%'
         OR r.repeatDays LIKE '%TUE%'
         OR r.repeatDays LIKE '%WED%'
         OR r.repeatDays LIKE '%THU%'
         OR r.repeatDays LIKE '%FRI%'
         OR r.repeatDays LIKE '%SAT%'
         OR r.repeatDays LIKE '%SUN%'
      )
    """)
    List<Routine> findWeeklyTargetRoutines(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

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
