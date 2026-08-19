package com.likelion.team4.domain.routine.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.team4.domain.routine.entity.enums.RepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "루틴 제목은 최대 100자까지 입력 가능합니다.")
    private String title;

    @Schema(type = "string", example = "07:30:00", description = "루틴 수행 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss") // (선택) 확실한 직렬화/역직렬화를 위해
    private LocalTime performTime;

    private String repeatDays;

    @NotNull(message = "알림 여부는 필수 입력값입니다.")
    private Boolean alarm;

    @NotNull(message = "활성화 여부는 필수 입력값입니다.")
    private Boolean active;

    @NotNull(message = "시작일은 필수 입력값입니다.")
    private LocalDate startDate;

    private LocalDate endDate;

    @Schema(type = "string", example = "07:20:00", description = "알람 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime alarmTime;

    @NotNull(message = "반복 타입은 필수 입력값입니다.")
    private RepeatType repeatType;

    private Integer repeatCount;
}