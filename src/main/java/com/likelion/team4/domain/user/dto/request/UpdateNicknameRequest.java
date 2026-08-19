package com.likelion.team4.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateNicknameRequest {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 입력 가능합니다.")
    private String nickname;

    public UpdateNicknameRequest(String nickname) {
        this.nickname = nickname;
    }
}
