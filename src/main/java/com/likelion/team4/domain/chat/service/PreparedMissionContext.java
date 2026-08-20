package com.likelion.team4.domain.chat.service;

import java.time.LocalDate;

/**
 * generateMission()의 DB 준비 단계(순수 조회) 결과를 담는 값 객체.
 * GPT API 호출(트랜잭션 밖)에 필요한 정보만 뽑아서 넘기기 위함.
 * 이전 PENDING 미션은 이 단계에서 거절 처리하지 않고, id만 넘겨서
 * saveMission() 단계에서 새 미션 저장과 함께 원자적으로 처리한다.
 */
public record PreparedMissionContext(
        Long chatId,
        String causeTag,
        LocalDate missionDate,
        Long pendingMissionId
) {
}
