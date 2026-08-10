package com.likelion.team4.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MissionActionRequest {

    @NotBlank(message = "action을 입력해주세요")
    private String action; // ACCEPT, REJECT
}
