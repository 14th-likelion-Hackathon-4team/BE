package com.likelion.team4.domain.Routine.entity;

import com.likelion.team4.domain.User.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
}