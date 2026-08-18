package com.likelion.team4.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ReportHistoryResponse {

    private Long reportId;
    private LocalDate date;
    private int completionRate;
    private int completedRoutineCount;
    private int totalRoutineCount;
    private int alternativeMissionCount;
}