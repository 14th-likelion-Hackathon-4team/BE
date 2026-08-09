package com.likelion.team4.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private AiChat aiChat;

    @Column(nullable = false, length = 10)
    private String role; // USER, AI

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 30)
    private String causeTag;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public AiChatMessage(AiChat aiChat, String role, String content, String causeTag) {
        this.aiChat = aiChat;
        this.role = role;
        this.content = content;
        this.causeTag = causeTag;
        this.createdAt = LocalDateTime.now();
    }
}
