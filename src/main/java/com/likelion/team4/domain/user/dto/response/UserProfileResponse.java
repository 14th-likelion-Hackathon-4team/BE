package com.likelion.team4.domain.user.dto.response;

import com.likelion.team4.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserProfileResponse {

    private final Long id;
    private final String loginId;
    private final String nickname;
    private final String status;
    private final int currentStreak;
    private final int maxStreak;
    private final boolean routineAlarmOn;
    private final boolean altMissionReminderOn;
    private final String alarmSound;
    private final String alarmOffsetType;
    private final Integer alarmOffsetMinutes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.nickname = user.getNickname();
        this.status = user.getStatus();
        this.currentStreak = user.getCurrentStreak();
        this.maxStreak = user.getMaxStreak();
        this.routineAlarmOn = user.isRoutineAlarmOn();
        this.altMissionReminderOn = user.isAltMissionReminderOn();
        this.alarmSound = user.getAlarmSound();
        this.alarmOffsetType = user.getAlarmOffsetType();
        this.alarmOffsetMinutes = user.getAlarmOffsetMinutes();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}