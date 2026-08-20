package com.likelion.team4.domain.chat.dto.response;

import com.likelion.team4.domain.chat.entity.AiChatMessage;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChatMessageResponse {

    private final Long id;
    private final String role;
    private final String content;
    private final String causeTag;
    private final LocalDateTime createdAt;

    public ChatMessageResponse(AiChatMessage message) {
        this.id = message.getId();
        this.role = message.getRole().name();
        this.content = message.getContent();
        this.causeTag = message.getCauseTag();
        this.createdAt = message.getCreatedAt();
    }
}
