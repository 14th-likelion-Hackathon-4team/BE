package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.dto.response.AlternativeMissionCompleteResponse;
import com.likelion.team4.domain.routine.entity.AlternativeMission;
import com.likelion.team4.domain.routine.repository.AlternativeMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AlternativeMissionService {

    private final AlternativeMissionRepository alternativeMissionRepository;

    public AlternativeMissionCompleteResponse completeMission(Long missionId) {

        AlternativeMission mission =
                alternativeMissionRepository.findById(missionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "대체 미션을 찾을 수 없습니다."
                                ));

        mission.complete();

        return AlternativeMissionCompleteResponse.builder()
                .missionId(missionId)
                .alternativeMissionCompleted(true)
                .completedAt(LocalDateTime.now())
                .build();
    }
}