package com.likelion.team4.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StreakResponse {

    private int currentStreak;
    private int maxStreak;
    private LocalDate lastCompletedDate;
    private LocalDate startedAt;
    private boolean isTodayCompleted;
}