package com.likelion.team4.domain.routine.controller;

import com.likelion.team4.domain.routine.dto.request.RoutineRequest;
import com.likelion.team4.domain.routine.dto.response.RoutineResponse;
import com.likelion.team4.domain.routine.service.RoutineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routinefit/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;

    @GetMapping
    public ResponseEntity<List<RoutineResponse>> getRoutinesByDay(
            // 임시로 설정(이후 JWT 토큰 이용할 예정)
            @RequestParam("userId") Long userId,
            @RequestParam(value = "day", required = false) String day) {
        List<RoutineResponse> routines;
        if (day != null && !day.trim().isEmpty()) {
            routines = routineService.getRoutinesByDay(userId, day);
        } else {
            routines = routineService.getAllRoutines(userId);
        }
        return ResponseEntity.ok(routines);
    }

    @PostMapping
    public ResponseEntity<RoutineResponse> createRoutine(
            @RequestParam("userId") Long userId,
            @RequestBody RoutineRequest request) {
        RoutineResponse response = routineService.createRoutine(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}