package com.likelion.team4.domain.chat.entity;

import com.likelion.team4.domain.chat.entity.enums.MissionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alternative_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlternativeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private AiChat aiChat;

    @Column(nullable = false, length = 500)
    private String content;

    private Integer durationMinutes;

    @Column(length = 10)
    private String difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDate missionDate;

    @Builder
    public AlternativeMission(
            AiChat aiChat,
            String content,
            Integer durationMinutes,
            String difficulty,
            LocalDate missionDate
    ) {
        this.aiChat = aiChat;
        this.content = content;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.status = MissionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.missionDate = missionDate;
    }

    public void accept() {
        this.status = MissionStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = MissionStatus.REJECTED;
    }

    public void complete() {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
