package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.dto.response.AlternativeMissionCompleteResponse;
import com.likelion.team4.domain.routine.entity.AlternativeMission;
import com.likelion.team4.domain.routine.repository.RoutineAlternativeMissionRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AlternativeMissionService {

    private final RoutineAlternativeMissionRepository alternativeMissionRepository;

    public AlternativeMissionCompleteResponse completeMission(Long missionId) {

        AlternativeMission mission =
                alternativeMissionRepository.findById(missionId)
                        .orElseThrow(() ->
                                new CustomException(ErrorCode.ALTERNATIVE_MISSION_NOT_FOUND)
                        );

        mission.complete();

        return AlternativeMissionCompleteResponse.builder()
                .missionId(missionId)
                .alternativeMissionCompleted(true)
                .completedAt(LocalDateTime.now())
                .build();
    }
}