package com.likelion.team4.domain.routine.controller;

import com.likelion.team4.domain.routine.dto.response.AlternativeMissionCompleteResponse;
import com.likelion.team4.domain.routine.service.AlternativeMissionService;
import com.likelion.team4.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routinefit/alternative-missions")
@RequiredArgsConstructor
public class AlternativeMissionController {

    private final AlternativeMissionService alternativeMissionService;

    @PatchMapping("/{missionId}/complete")
    public ResponseEntity<ApiResponse<AlternativeMissionCompleteResponse>> completeMission(
            @PathVariable Long missionId
    ) {
        AlternativeMissionCompleteResponse response =
                alternativeMissionService.completeMission(missionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "대체 미션이 완료되었습니다.",
                        response
                )
        );
    }
}