package com.likelion.team4.domain.chat.dto.response;

import com.likelion.team4.domain.chat.entity.AlternativeMission;
import lombok.Getter;

@Getter
public class MissionResponse {

    private final Long missionId;
    private final String content;
    private final Integer durationMinutes;
    private final String durationLabel;
    private final String difficulty;
    private final String status;

    public MissionResponse(AlternativeMission mission) {
        this.missionId = mission.getId();
        this.content = mission.getContent();
        this.durationMinutes = mission.getDurationMinutes();
        this.durationLabel = formatDuration(mission.getDurationMinutes());
        this.difficulty = mission.getDifficulty();
        this.status = mission.getStatus().name();
    }

    private static String formatDuration(Integer minutes) {
        if (minutes == null) {
            return null;
        }
        int hours = minutes / 60;
        int remaining = minutes % 60;
        if (hours == 0) {
            return remaining + "분";
        }
        if (remaining == 0) {
            return hours + "시간";
        }
        return hours + "시간 " + remaining + "분";
    }
}
