package com.likelion.team4.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WeeklyReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;

    private int totalRoutineCount;
    private int completedRoutineCount;
    private int completionRate;

    private LocalDate bestDay;
    private int currentStreak;

    private List<WeeklyDailyReportResponse> dailyReports;
}