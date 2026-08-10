package com.likelion.team4.domain.main.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchRoutineResponse {

    private Long routineId;
    private String title;
}