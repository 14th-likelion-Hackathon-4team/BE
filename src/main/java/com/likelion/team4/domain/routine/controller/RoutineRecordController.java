package com.likelion.team4.domain.routine.controller;

import com.likelion.team4.domain.routine.dto.response.RoutineCompleteResponse;
import com.likelion.team4.domain.routine.service.RoutineRecordService;
import com.likelion.team4.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routinefit/routines")
@RequiredArgsConstructor
public class RoutineRecordController {

    private final RoutineRecordService routineRecordService;

    @PatchMapping("/{routineId}/complete")
    public ResponseEntity<ApiResponse<RoutineCompleteResponse>> completeRoutine(
            @PathVariable Long routineId
    ) {
        RoutineCompleteResponse response =
                routineRecordService.completeRoutine(routineId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "루틴이 완료되었습니다.",
                        response
                )
        );
    }
}