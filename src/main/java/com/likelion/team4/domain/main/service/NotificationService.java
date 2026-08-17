package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.main.entity.Notification;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(
            User user,
            Routine routine,
            String content
    ) {
        Notification notification = Notification.builder()
                .user(user)
                .routine(routine)
                .content(content)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }
}