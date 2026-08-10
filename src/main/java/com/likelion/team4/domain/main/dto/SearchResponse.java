package com.likelion.team4.domain.main.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponse {

    private List<SearchRoutineResponse> routines;
    private List<SearchNotificationResponse> notifications;
}