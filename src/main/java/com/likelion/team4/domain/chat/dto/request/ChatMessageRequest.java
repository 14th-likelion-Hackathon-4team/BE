package com.likelion.team4.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    @NotBlank(message = "원인 태그를 입력해주세요")
    private String causeTag; // 약속, 피로, 시간부족, 기분, 기타
}
