package com.likelion.team4.domain.routine.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.team4.domain.routine.entity.Routine;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class RoutineResponse {
    private Long id;
    private String title;
    @Schema(type = "string", example = "07:30:00", description = "루틴 수행 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime performTime;
    private String repeatDays;
    private boolean alarm;
    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;
    @Schema(type = "string", example = "07:20:00", description = "알람 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime alarmTime;
    private String repeatType;
    private Integer repeatCount;

    public static RoutineResponse from(Routine routine) {
        return RoutineResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .performTime(routine.getPerformTime())
                .repeatDays(routine.getRepeatDays())
                .alarm(routine.isAlarm())
                .active(routine.isActive())
                .startDate(routine.getStartDate())
                .endDate(routine.getEndDate())
                .alarmTime(routine.getAlarmTime())
                .repeatType(routine.getRepeatType())
                .repeatCount(routine.getRepeatCount())
                .build();
    }
}
