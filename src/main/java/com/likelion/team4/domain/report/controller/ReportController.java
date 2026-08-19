package com.likelion.team4.domain.report.controller;

import com.likelion.team4.domain.report.dto.response.DailyReportResponse;
import com.likelion.team4.domain.report.dto.response.ReportHistoryResponse;
import com.likelion.team4.domain.report.dto.response.StreakResponse;
import com.likelion.team4.domain.report.dto.response.WeeklyReportResponse;
import com.likelion.team4.domain.report.service.ReportService;
import com.likelion.team4.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routinefit/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyReportResponse>> getDailyReport(
            @RequestParam Long userId,
            @RequestParam LocalDate date
    ) {

        DailyReportResponse response =
                reportService.getDailyReport(userId, date);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "일간 리포트 조회 성공",
                        response
                )
        );
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport(
            @RequestParam Long userId,
            @RequestParam LocalDate date
    ) {
        WeeklyReportResponse response =
                reportService.getWeeklyReport(userId, date);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "주간 리포트 조회 성공",
                        response
                )
        );
    }

    @GetMapping("/streak")
    public ResponseEntity<ApiResponse<StreakResponse>> getStreak(
            @RequestParam Long userId
    ) {
        StreakResponse response = reportService.getStreak(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "연속 기록 조회 성공",
                        response
                )
        );
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ReportHistoryResponse>>> getReportHistory(
            @RequestParam Long userId
    ) {
        List<ReportHistoryResponse> response =
                reportService.getReportHistory(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "조회 성공",
                        response
                )
        );
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<DailyReportResponse>> getReportDetail(
            @RequestParam Long userId,
            @PathVariable Long reportId
    ) {
        DailyReportResponse response =
                reportService.getReportDetail(userId, reportId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "조회 성공",
                        response
                )
        );
    }
}