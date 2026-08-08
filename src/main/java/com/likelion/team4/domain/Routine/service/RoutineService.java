package com.likelion.team4.domain.Routine.service;

import com.likelion.team4.domain.Routine.dto.RoutineResponse;
import com.likelion.team4.domain.Routine.entity.Routine;
import com.likelion.team4.domain.Routine.repository.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

    private final RoutineRepository routineRepository;

    public List<RoutineResponse> getRoutinesByDay(Long userId, String day) {
        // 1. 레포지토리에서 유저 ID를 기준으로 루틴 엔티티 리스트 조회
        List<Routine> routines = routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(userId, day);
        // 2. 조회된 엔티티 리스트를 DTO 리스트로 변환하여 반환
        return routines.stream()
                .map(RoutineResponse::from)
                .toList();
    }

    public List<RoutineResponse> getAllRoutines(Long userId) {
        List<Routine> routines = routineRepository.findAllByUser_IdAndDeletedAtIsNull(userId);
        return routines.stream()
                .map(RoutineResponse::from)
                .toList();
    }
}
