package com.likelion.team4.domain.routine.controller;

import com.likelion.team4.domain.routine.dto.request.RoutineActiveRequest;
import com.likelion.team4.domain.routine.dto.request.RoutineRequest;
import com.likelion.team4.domain.routine.dto.response.RoutineResponse;
import com.likelion.team4.domain.routine.service.RoutineService;
import com.likelion.team4.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routinefit/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoutineResponse>>> getRoutinesByDay(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "day", required = false) String day) {
        List<RoutineResponse> routines;
        if (day != null && !day.trim().isEmpty()) {
            routines = routineService.getRoutinesByDay(userId, day);
        } else {
            routines = routineService.getAllRoutines(userId);
        }
        return ResponseEntity.ok(ApiResponse.success("S200", "루틴 목록 조회 성공", routines));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoutineResponse>> createRoutine(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RoutineRequest request) {
        RoutineResponse response = routineService.createRoutine(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("S201", "루틴 생성 성공", response));
    }

    @GetMapping("/{routineId}")
    public ResponseEntity<ApiResponse<RoutineResponse>> getDetailRoutine(
            @AuthenticationPrincipal Long userId,
            @PathVariable("routineId") Long routineId) {

        RoutineResponse response = routineService.getRoutine(userId, routineId);
        return ResponseEntity.ok(ApiResponse.success("S200", "루틴 상세 조회 성공", response));
    }

    @PatchMapping("/{routineId}")
    public ResponseEntity<ApiResponse<RoutineResponse>> updateRoutine(
            @AuthenticationPrincipal Long userId,
            @PathVariable("routineId") Long routineId,
            @Valid @RequestBody RoutineRequest request) {

        RoutineResponse response = routineService.updateRoutine(userId, routineId, request);
        return ResponseEntity.ok(ApiResponse.success("S200", "루틴 수정 성공", response));
    }

    @PatchMapping("/{routineId}/status")
    public ResponseEntity<ApiResponse<RoutineResponse>> updateRoutineActiveStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable("routineId") Long routineId,
            @Valid @RequestBody RoutineActiveRequest request) {

        RoutineResponse response = routineService.updateActiveStatus(userId, routineId, request.getActive());
        return ResponseEntity.ok(ApiResponse.success("S200", "루틴 활성화 상태 변경 성공", response));
    }
}