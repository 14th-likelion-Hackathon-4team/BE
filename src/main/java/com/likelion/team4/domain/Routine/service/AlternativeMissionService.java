package com.likelion.team4.domain.Routine.service;

import com.likelion.team4.domain.Routine.entity.AlternativeMission;
import com.likelion.team4.domain.Routine.repository.AlternativeMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlternativeMissionService {

    private final AlternativeMissionRepository alternativeMissionRepository;

    public void completeMission(Long missionId) {

        AlternativeMission mission =
                alternativeMissionRepository.findById(missionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "대체 미션을 찾을 수 없습니다."
                                ));

        mission.complete();
    }
}