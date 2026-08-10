package com.likelion.team4.domain.main.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TodayNotificationResponse {

    private Long notificationId;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
}