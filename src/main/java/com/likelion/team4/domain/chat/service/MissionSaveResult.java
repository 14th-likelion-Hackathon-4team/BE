package com.likelion.team4.domain.chat.service;

import com.likelion.team4.domain.chat.dto.response.MissionResponse;
import com.likelion.team4.domain.chat.entity.AlternativeMission;

/**
 * saveMission()의 결과. 새로 생성된 미션과, 함께 거절 처리된 이전 PENDING 미션(있었다면)을 담는다.
 */
public record MissionSaveResult(
        AlternativeMission newMission,
        MissionResponse previousMission
) {
}
