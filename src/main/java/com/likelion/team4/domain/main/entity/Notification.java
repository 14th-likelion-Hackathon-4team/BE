package com.likelion.team4.domain.main.entity;

import com.likelion.team4.domain.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
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

    // 알림 내용
    @Column(nullable = false, length = 255)
    private String content;

    // 읽음 여부
    @Column(nullable = false)
    private boolean read;

    // 생성 시간
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 알림 읽음 처리
    public void read() {
        this.read = true;
    }
}