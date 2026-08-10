package com.likelion.team4.domain.routine.repository;

import com.likelion.team4.domain.routine.entity.AlternativeMission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternativeMissionRepository
        extends JpaRepository<AlternativeMission, Long> {
}