package com.likelion.team4.domain.routine.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineRequest {
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
}