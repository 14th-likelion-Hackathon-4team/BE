package com.likelion.team4.domain.chat.dto.response;

import lombok.Getter;

@Getter
public class MissionActionResponse {

    private final Long missionId;
    private final String status;
    private final String routineLogStatus;

    public MissionActionResponse(Long missionId, String status, String routineLogStatus) {
        this.missionId = missionId;
        this.status = status;
        this.routineLogStatus = routineLogStatus;
    }
}
