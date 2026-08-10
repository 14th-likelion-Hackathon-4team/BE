package com.likelion.team4.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false, length = 20)
    private String status; // PENDING, ACCEPTED, REJECTED, COMPLETED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;

    @Builder
    public AlternativeMission(AiChat aiChat, String content, Integer durationMinutes, String difficulty) {
        this.aiChat = aiChat;
        this.content = content;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = "ACCEPTED";
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = "REJECTED";
    }

    public void complete() {
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
}
