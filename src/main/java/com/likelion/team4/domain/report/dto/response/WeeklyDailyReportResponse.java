package com.likelion.team4.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class WeeklyDailyReportResponse {

    private LocalDate date;
    private int completionRate;
}