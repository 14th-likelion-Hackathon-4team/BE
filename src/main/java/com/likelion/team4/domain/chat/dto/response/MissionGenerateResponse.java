package com.likelion.team4.domain.chat.dto.response;

import lombok.Getter;

@Getter
public class MissionGenerateResponse {

    private final MissionResponse mission;
    private final MissionResponse previousMission;

    public MissionGenerateResponse(MissionResponse mission, MissionResponse previousMission) {
        this.mission = mission;
        this.previousMission = previousMission;
    }
}
