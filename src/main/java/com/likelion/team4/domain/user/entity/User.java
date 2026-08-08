package com.likelion.team4.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, WITHDRAWN

    @Column(nullable = false)
    private Boolean routineAlarmOn;

    @Column(nullable = false)
    private Boolean altMissionReminderOn;

    @Column(nullable = false, length = 20)
    private String alarmSound;

    @Column(nullable = false, length = 20)
    private String alarmOffsetType;

    private Integer alarmOffsetMinutes;

    @Column(nullable = false)
    private Integer currentStreak;

    @Column(nullable = false)
    private Integer maxStreak;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

    private LocalDateTime withdrawnAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String loginId, String password, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.status = "ACTIVE";
        this.routineAlarmOn = true;
        this.altMissionReminderOn = true;
        this.alarmSound = "차분한벨";
        this.alarmOffsetType = "1시간전";
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }
}
