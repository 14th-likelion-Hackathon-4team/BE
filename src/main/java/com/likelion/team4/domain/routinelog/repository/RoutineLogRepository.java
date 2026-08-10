package com.likelion.team4.domain.routinelog.repository;

import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, Long> {
}