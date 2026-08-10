package com.likelion.team4.domain.routine.entity;

import com.likelion.team4.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Routines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User 엔티티와의 연관관계 매핑 (FK: user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "perform_time")
    private LocalTime performTime;

    @Column(name = "repeat_days", nullable = false, length = 20)
    private String repeatDays;

    @Column(nullable = false)
    private boolean alarm;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "alarm_time")
    private LocalTime alarmTime;

    @Column(name = "repeat_type", nullable = false, length = 20)
    private String repeatType;

    @Column(name = "repeat_count")
    private Integer repeatCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Routine(User user, String title, LocalTime performTime, String repeatDays, boolean alarm, boolean active, LocalDate startDate, LocalDate endDate, LocalTime alarmTime, String repeatType, Integer repeatCount) {
        this.user = user;
        this.title = title;
        this.performTime = performTime;
        this.repeatDays = repeatDays;
        this.alarm = alarm;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.alarmTime = alarmTime;
        this.repeatType = repeatType;
        this.repeatCount = repeatCount;
    }

    public void update(String title, LocalTime performTime, String repeatDays, boolean alarm, boolean active, LocalDate startDate, LocalDate endDate, LocalTime alarmTime, String repeatType, Integer repeatCount) {
        this.title = title;
        this.performTime = performTime;
        this.repeatDays = repeatDays;
        this.alarm = alarm;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.alarmTime = alarmTime;
        this.repeatType = repeatType;
        this.repeatCount = repeatCount;
    }
}