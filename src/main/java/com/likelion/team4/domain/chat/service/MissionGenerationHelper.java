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

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * generateMission()의 DB 조회/저장 구간을 GPT API 호출(외부 네트워크, 최대 10초 블로킹)과
 * 분리하기 위한 헬퍼. GPT 호출 동안 DB 트랜잭션/커넥션을 붙잡고 있지 않도록 함.
 *
 * prepare()는 순수 조회만 수행하고, 이전 PENDING 미션의 거절 처리는 saveMission()에서
 * 새 미션 저장과 같은 트랜잭션으로 묶어서 처리한다. (GPT 호출이 실패해도 이전 미션이
 * 거절된 채로 남아 새 미션 없이 유실되는 원자성 문제를 방지)
 */
@Component
@RequiredArgsConstructor
public class MissionGenerationHelper {

    private final AiChatRepository aiChatRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final AlternativeMissionRepository alternativeMissionRepository;

    @Transactional(readOnly = true)
    public PreparedMissionContext prepare(Long chatId) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        Long pendingMissionId = alternativeMissionRepository
                .findByAiChatIdAndStatus(chatId, MissionStatus.PENDING)
                .map(AlternativeMission::getId)
                .orElse(null);

        // 대화에서 원인 태그 + 사용자가 직접 쓴 설명 가져오기
        Optional<AiChatMessage> firstUserMessage = aiChatMessageRepository
                .findFirstByAiChatIdAndRoleOrderByCreatedAtAsc(chatId, MessageRole.USER);

        String causeTag = firstUserMessage.map(AiChatMessage::getCauseTag).orElse("기타");
        String userReason = firstUserMessage.map(AiChatMessage::getContent).orElse("");

        return new PreparedMissionContext(
                chatId,
                chat.getRoutineLog().getRoutine().getTitle(),
                causeTag,
                userReason,
                chat.getRoutineLog().getLogDate(),
                pendingMissionId
        );
    }

    @Transactional
    public MissionSaveResult saveMission(
            Long chatId,
            Long pendingMissionId,
            Map<String, Object> missionData,
            LocalDate missionDate
    ) {
        MissionResponse previousMission = null;
        if (pendingMissionId != null) {
            Optional<AlternativeMission> pendingMission = alternativeMissionRepository.findById(pendingMissionId);
            if (pendingMission.isPresent() && pendingMission.get().getStatus() == MissionStatus.PENDING) {
                pendingMission.get().reject();
                previousMission = new MissionResponse(pendingMission.get());
            }
        }

        AiChat chat = aiChatRepository.getReferenceById(chatId);

        AlternativeMission newMission = AlternativeMission.builder()
                .aiChat(chat)
                .content((String) missionData.get("content"))
                .durationMinutes((Integer) missionData.get("durationMinutes"))
                .difficulty((String) missionData.get("difficulty"))
                .missionDate(missionDate)
                .build();
        alternativeMissionRepository.save(newMission);

        return new MissionSaveResult(newMission, previousMission);
    }
}
