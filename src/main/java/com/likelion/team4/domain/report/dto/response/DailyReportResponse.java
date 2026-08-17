package com.likelion.team4.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class DailyReportResponse {

    private LocalDate date;
    private int totalRoutineCount;
    private int completedRoutineCount;
    private int alternativeMissionCount;
    private int completionRate;
    private int currentStreak;
    private List<DailyRoutineResponse> routines;
}