package com.likelion.team4.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

import lombok.Builder;

@Entity
@Table(name = "Users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    @Column(name = "max_streak", nullable = false)
    private int maxStreak;

    @Column(name = "routine_alarm_on", nullable = false)
    private boolean routineAlarmOn;

    @Column(name = "alt_mission_reminder_on", nullable = false)
    private boolean altMissionReminderOn;

    @Column(name = "alarm_sound", nullable = false, length = 20)
    private String alarmSound;

    @Column(name = "alarm_offset_type", nullable = false, length = 20)
    private String alarmOffsetType;

    // NULL 허용 컬럼은 객체 타입(Integer) 사용
    @Column(name = "alarm_offset_minutes")
    private Integer alarmOffsetMinutes;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    public void updateRefreshToken(
            String refreshToken,
            LocalDateTime refreshTokenExpiresAt
    ) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}