package com.likelion.team4.domain.routine.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "타이틀은 필수 입력값입니다.")
    private String title;

    private LocalTime performTime;

    @NotBlank(message = "반복 요일은 필수 입력값입니다.")
    private String repeatDays;

    @NotNull(message = "알림 여부는 필수 입력값입니다.")
    private Boolean alarm;

    @NotNull(message = "활성화 여부는 필수 입력값입니다.")
    private Boolean active;

    @NotNull(message = "시작일은 필수 입력값입니다.")
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime alarmTime;

    @NotBlank(message = "반복 타입은 필수 입력값입니다.")
    private String repeatType;

    private Integer repeatCount;
}