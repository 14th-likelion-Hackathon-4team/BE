package com.likelion.team4.domain.routine.repository;

import com.likelion.team4.domain.routine.entity.AlternativeMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RoutineAlternativeMissionRepository
        extends JpaRepository<AlternativeMission, Long> {

    void deleteAllByRoutine_User_Id(Long userId);

    Optional<AlternativeMission> findByRoutine_IdAndMissionDate(
            Long routineId,
            LocalDate missionDate
    );
}