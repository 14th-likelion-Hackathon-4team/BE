package com.likelion.team4.domain.report.service;

import com.likelion.team4.domain.report.dto.response.DailyReportResponse;
import com.likelion.team4.domain.report.dto.response.DailyRoutineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    public DailyReportResponse getDailyReport(LocalDate date) {

        // TODO
        // 1. 해당 날짜의 루틴 조회
        // 2. 해당 날짜의 루틴 완료 기록 조회
        // 3. 대체 미션 개수 조회
        // 4. 연속 기록 계산
        // 5. 응답 DTO 생성

        return DailyReportResponse.builder()
                .date(date)
                .totalRoutineCount(0)
                .completedRoutineCount(0)
                .alternativeMissionCount(0)
                .completionRate(0)
                .currentStreak(0)
                .routines(List.of())
                .build();
    }
}