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

    private String title;

    private LocalTime performTime;

    private boolean completed;
}