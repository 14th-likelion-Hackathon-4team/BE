package com.likelion.team4.domain.user.dto.response;

import com.likelion.team4.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SignupResponse {

    private final Long id;
    private final String loginId;
    private final String nickname;
    private final LocalDateTime createdAt;

    public SignupResponse(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.nickname = user.getNickname();
        this.createdAt = user.getCreatedAt();
    }
}
