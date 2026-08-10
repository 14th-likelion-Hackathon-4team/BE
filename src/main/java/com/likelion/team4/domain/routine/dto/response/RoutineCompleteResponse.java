package com.likelion.team4.domain.routine.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoutineCompleteResponse {

    private Long routineId;
    private boolean completed;
    private LocalDateTime completedAt;
}