package com.likelion.team4.domain.main.entity;

import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받는 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 알림과 연결된 루틴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;

    // 알림 내용
    @Column(nullable = false, length = 255)
    private String content;

    // 읽음 여부
    @Column(name = "is_read", nullable = false)
    private boolean read;

    // 생성 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 알림 읽음 처리
    public void read() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    @Column(name = "read_at")
    private LocalDateTime readAt;
}