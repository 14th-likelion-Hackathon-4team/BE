package com.likelion.team4.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class TodayRoutineResponse {

    private Long routineId;

    private Long routineLogId;

    private String routineName;

    private LocalTime scheduledTime;

    private boolean completed;
}