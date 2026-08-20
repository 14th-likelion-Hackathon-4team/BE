package com.likelion.team4.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class TodayRoutineResponse {

    private Long routineId;

    private String routineName;

    private LocalTime scheduledTime;

    private boolean completed;

    // 전체 루틴의 최종 상태
    private String routineStatus;

    // 대체 미션 정보
    private Long alternativeMissionId;

    private String alternativeMissionTitle;

    private String alternativeMissionStatus;

    private Boolean alternativeMissionCompleted;

    private LocalDateTime alternativeMissionCompletedAt;
}