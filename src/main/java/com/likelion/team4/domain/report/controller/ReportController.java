package com.likelion.team4.domain.report.controller;

import com.likelion.team4.domain.report.dto.response.DailyReportResponse;
import com.likelion.team4.domain.report.service.ReportService;
import com.likelion.team4.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routinefit/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyReportResponse>> getDailyReport(
            @RequestParam LocalDate date
    ) {
        DailyReportResponse response = reportService.getDailyReport(date);

        return ResponseEntity.ok(
                ApiResponse.success("S200", "조회 성공", response)
        );
    }
}