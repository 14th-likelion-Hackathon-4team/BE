package com.likelion.team4.domain.user.dto.response;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final UserInfo user;

    public LoginResponse(String accessToken, String refreshToken, Long id, String nickname) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = new UserInfo(id, nickname);
    }

    @Getter
    public static class UserInfo {
        private final Long id;
        private final String nickname;

        public UserInfo(Long id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }
    }
}
