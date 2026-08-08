package com.likelion.team4.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용할 수 있습니다")
    @Size(max = 50, message = "아이디는 50자 이하로 입력해주세요")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상 입력해주세요")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(max = 20, message = "닉네임은 20자 이하로 입력해주세요")
    private String nickname;
}
