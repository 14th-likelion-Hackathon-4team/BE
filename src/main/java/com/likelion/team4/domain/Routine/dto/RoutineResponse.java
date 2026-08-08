package com.likelion.team4.domain.Routine.dto;

import com.likelion.team4.domain.Routine.entity.Routine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class RoutineResponse {
    private Long id;
    private String title;
    private LocalTime performTime;
    private String repeatDays;
    private boolean alarm;
    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;
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
