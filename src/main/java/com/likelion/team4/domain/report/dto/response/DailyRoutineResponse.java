package com.likelion.team4.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyRoutineResponse {

    private Long routineId;
    private String title;
    private boolean completed;
}