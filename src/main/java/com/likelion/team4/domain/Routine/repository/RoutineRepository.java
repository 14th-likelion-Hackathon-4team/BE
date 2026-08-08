package com.likelion.team4.domain.Routine.repository;
import com.likelion.team4.domain.Routine.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findAllByUser_IdAndDeletedAtIsNull(Long userId);
    List<Routine> findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(Long userId, String day);
}
