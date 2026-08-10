package com.likelion.team4.domain.routine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AlternativeMissionCompleteResponse {

    private Long missionId;
    private boolean alternativeMissionCompleted;
    private LocalDateTime completedAt;
}