package com.likelion.team4.domain.Routine.controller;

import com.likelion.team4.domain.Routine.service.AlternativeMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routinefit/alternative-missions")
@RequiredArgsConstructor
public class AlternativeMissionController {

    private final AlternativeMissionService alternativeMissionService;

    @PatchMapping("/{missionId}/complete")
    public ResponseEntity<Void> completeMission(
            @PathVariable Long missionId
    ) {
        alternativeMissionService.completeMission(missionId);

        return ResponseEntity.noContent().build();
    }
}