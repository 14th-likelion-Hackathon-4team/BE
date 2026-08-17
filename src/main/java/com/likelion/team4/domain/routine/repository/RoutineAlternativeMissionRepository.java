package com.likelion.team4.domain.routine.repository;

import com.likelion.team4.domain.routine.entity.AlternativeMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineAlternativeMissionRepository
        extends JpaRepository<AlternativeMission, Long> {
    void deleteAllByRoutine_User_Id(Long userId);
}