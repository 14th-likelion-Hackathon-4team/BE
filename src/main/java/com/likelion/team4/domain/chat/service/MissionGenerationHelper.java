package com.likelion.team4.domain.chat.service;

import com.likelion.team4.domain.chat.dto.response.MissionResponse;
import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.entity.AiChatMessage;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.entity.enums.MessageRole;
import com.likelion.team4.domain.chat.entity.enums.MissionStatus;
import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * generateMission()의 DB 조회/저장 구간을 GPT API 호출(외부 네트워크, 최대 10초 블로킹)과
 * 분리하기 위한 헬퍼. GPT 호출 동안 DB 트랜잭션/커넥션을 붙잡고 있지 않도록 함.
 */
@Component
@RequiredArgsConstructor
public class MissionGenerationHelper {

    private final AiChatRepository aiChatRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final AlternativeMissionRepository alternativeMissionRepository;

    @Transactional
    public PreparedMissionContext prepare(Long chatId) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 이전 PENDING 미션 REJECTED 처리
        Optional<AlternativeMission> pendingMission = alternativeMissionRepository
                .findByAiChatIdAndStatus(chatId, MissionStatus.PENDING);

        MissionResponse previousMission = null;
        if (pendingMission.isPresent()) {
            pendingMission.get().reject();
            previousMission = new MissionResponse(pendingMission.get());
        }

        // 대화에서 원인 태그 가져오기
        String causeTag = aiChatMessageRepository
                .findFirstByAiChatIdAndRoleOrderByCreatedAtAsc(chatId, MessageRole.USER)
                .map(AiChatMessage::getCauseTag)
                .orElse("기타");

        return new PreparedMissionContext(
                chatId,
                causeTag,
                chat.getRoutineLog().getLogDate(),
                previousMission
        );
    }

    @Transactional
    public AlternativeMission saveMission(Long chatId, Map<String, Object> missionData, java.time.LocalDate missionDate) {
        AiChat chat = aiChatRepository.getReferenceById(chatId);

        AlternativeMission newMission = AlternativeMission.builder()
                .aiChat(chat)
                .content((String) missionData.get("content"))
                .durationMinutes((Integer) missionData.get("durationMinutes"))
                .difficulty((String) missionData.get("difficulty"))
                .missionDate(missionDate)
                .build();

        return alternativeMissionRepository.save(newMission);
    }
}
