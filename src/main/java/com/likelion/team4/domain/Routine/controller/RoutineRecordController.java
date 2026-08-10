package com.likelion.team4.domain.Routine.controller;

import com.likelion.team4.domain.Routine.service.RoutineRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routinefit/routines")
@RequiredArgsConstructor
public class RoutineRecordController {

    private final RoutineRecordService routineRecordService;

    @PatchMapping("/{routineId}/complete")
    public ResponseEntity<Void> completeRoutine(
            @PathVariable Long routineId
    ) {
        routineRecordService.completeRoutine(routineId);

        return ResponseEntity.noContent().build();
    }
}