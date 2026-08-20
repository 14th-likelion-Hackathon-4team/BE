package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.dto.response.AlternativeMissionCompleteResponse;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlternativeMissionService {

    private final AlternativeMissionRepository alternativeMissionRepository;

    public AlternativeMissionCompleteResponse completeMission(Long missionId) {

        AlternativeMission mission =
                alternativeMissionRepository.findById(missionId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.ALTERNATIVE_MISSION_NOT_FOUND
                                )
                        );

        mission.complete();

        return AlternativeMissionCompleteResponse.builder()
                .missionId(mission.getId())
                .alternativeMissionCompleted(true)
                .completedAt(mission.getCompletedAt())
                .build();
    }
}