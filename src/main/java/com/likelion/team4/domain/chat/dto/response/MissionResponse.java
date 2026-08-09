package com.likelion.team4.domain.chat.dto.response;

import com.likelion.team4.domain.chat.entity.AlternativeMission;
import lombok.Getter;

@Getter
public class MissionResponse {

    private final Long missionId;
    private final String content;
    private final Integer durationMinutes;
    private final String difficulty;
    private final String status;

    public MissionResponse(AlternativeMission mission) {
        this.missionId = mission.getId();
        this.content = mission.getContent();
        this.durationMinutes = mission.getDurationMinutes();
        this.difficulty = mission.getDifficulty();
        this.status = mission.getStatus();
    }
}
