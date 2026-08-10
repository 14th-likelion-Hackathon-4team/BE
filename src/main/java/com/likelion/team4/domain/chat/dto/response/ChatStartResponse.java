package com.likelion.team4.domain.chat.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ChatStartResponse {

    private final Long chatId;
    private final Long routineLogId;
    private final boolean isNew;
    private final List<ChatMessageResponse> messages;

    public ChatStartResponse(Long chatId, Long routineLogId, boolean isNew, List<ChatMessageResponse> messages) {
        this.chatId = chatId;
        this.routineLogId = routineLogId;
        this.isNew = isNew;
        this.messages = messages;
    }
}
