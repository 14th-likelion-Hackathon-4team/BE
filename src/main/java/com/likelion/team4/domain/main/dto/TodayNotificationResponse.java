package com.likelion.team4.domain.main.dto;

import com.likelion.team4.domain.main.entity.Notification;
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

    public static TodayNotificationResponse from(Notification notification) {
        return TodayNotificationResponse.builder()
                .notificationId(notification.getId())
                .content(notification.getContent())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}