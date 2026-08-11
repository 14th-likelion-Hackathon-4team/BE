package com.likelion.team4.domain.routine.repository;
import com.likelion.team4.domain.routine.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findAllByUser_IdAndDeletedAtIsNull(Long userId);
    List<Routine> findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(Long userId, String day);
    Optional<Routine> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);
}
