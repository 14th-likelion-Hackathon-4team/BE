package com.likelion.team4.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationReadResponse {

    private Long notificationId;
    private boolean isRead;
    private LocalDateTime readAt;
}