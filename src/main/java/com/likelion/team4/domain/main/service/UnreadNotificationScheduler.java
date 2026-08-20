package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnreadNotificationScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final NotificationAiService notificationAiService;


    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void resendUnreadNotifications() {

        LocalDateTime now = LocalDateTime.now(SEOUL_ZONE);

        log.info(
                "=== 미읽음 알림 재전송 검사 시작 === now={}",
                now
        );

        // 최근 알림들을 가져와 루틴별 마지막 알림을 확인
        List<Notification> notifications =
                notificationRepository.findAllUnreadNotifications();

        for (Notification notification : notifications) {

            if (notification.getRoutine() == null) {
                continue;
            }

            Long routineId = notification.getRoutine().getId();

            // 해당 루틴의 가장 최근 알림 확인
            Notification latestNotification =
                    notificationRepository
                            .findTopByRoutine_IdOrderByCreatedAtDesc(
                                    routineId
                            )
                            .orElse(null);

            if (latestNotification == null) {
                continue;
            }

            // 최신 알림이 읽혔다면 재알림하지 않음
            if (latestNotification.isRead()) {
                continue;
            }

            // 최신 알림이 1시간 이상 지났는지 확인
            LocalDateTime oneHourAgo =
                    now.minusHours(1);

            if (latestNotification.getCreatedAt().isAfter(oneHourAgo)) {
                continue;
            }

            String content =
                    notificationAiService
                            .generateNotification(
                                    latestNotification.getRoutine()
                            );

            notificationService.createNotification(
                    latestNotification.getUser(),
                    latestNotification.getRoutine(),
                    content
            );

            log.info(
                    "========== 미읽음 알림 재전송 ==========" +
                            " previousNotificationId={}, routineId={}, userId={}",
                    latestNotification.getId(),
                    routineId,
                    latestNotification.getUser().getId()
            );
        }
    }
}