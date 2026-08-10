package com.likelion.team4.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class TodayNotificationListResponse {

    private boolean hasUnread;

    private List<TodayNotificationResponse> notifications;
}