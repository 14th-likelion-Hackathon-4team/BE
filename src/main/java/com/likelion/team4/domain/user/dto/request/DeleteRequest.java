package com.likelion.team4.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRequest {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
