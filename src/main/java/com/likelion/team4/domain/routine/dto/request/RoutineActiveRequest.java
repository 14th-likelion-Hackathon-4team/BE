package com.likelion.team4.domain.routine.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoutineActiveRequest {

    @NotNull(message = "활성화 여부는 필수 입력값입니다.")
    private Boolean active;
}