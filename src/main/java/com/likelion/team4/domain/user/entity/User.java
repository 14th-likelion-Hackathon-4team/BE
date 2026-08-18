package com.likelion.team4.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public void updateRefreshToken(String refreshToken, LocalDateTime refreshTokenExpiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAlarmSettings(boolean routineAlarmOn, boolean altMissionReminderOn,
                                    String alarmSound, String alarmOffsetType, Integer alarmOffsetMinutes) {
        this.routineAlarmOn = routineAlarmOn;
        this.altMissionReminderOn = altMissionReminderOn;
        this.alarmSound = alarmSound;
        this.alarmOffsetType = alarmOffsetType;
        this.alarmOffsetMinutes = alarmOffsetMinutes;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStreak(int currentStreak) {
        this.currentStreak = currentStreak;

        if (currentStreak > this.maxStreak) {
            this.maxStreak = currentStreak;
        }

        this.updatedAt = LocalDateTime.now();
    }
}