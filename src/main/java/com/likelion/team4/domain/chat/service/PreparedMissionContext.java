package com.likelion.team4.domain.chat.service;

import com.likelion.team4.domain.chat.dto.response.MissionResponse;

import java.time.LocalDate;

/**
 * generateMission()의 DB 준비 단계 결과를 담는 값 객체.
 * GPT API 호출(트랜잭션 밖)에 필요한 정보만 뽑아서 넘기기 위함.
 */
public record PreparedMissionContext(
        Long chatId,
        String causeTag,
        LocalDate missionDate,
        MissionResponse previousMission
) {
}
