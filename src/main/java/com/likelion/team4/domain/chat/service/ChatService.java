package com.likelion.team4.domain.chat.service;

import com.likelion.team4.domain.chat.dto.request.ChatMessageRequest;
import com.likelion.team4.domain.chat.dto.request.MissionActionRequest;
import com.likelion.team4.domain.chat.dto.response.*;
import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.entity.AiChatMessage;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final AiChatRepository aiChatRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final AlternativeMissionRepository alternativeMissionRepository;
    private final RoutineLogRepository routineLogRepository;

    // 1. 대화 시작
    @Transactional
    public ChatStartResponse startChat(Long routineLogId) {
        RoutineLog routineLog = routineLogRepository.findById(routineLogId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 기존 대화 확인 → 있으면 재사용
        Optional<AiChat> existingChat = aiChatRepository
                .findTopByRoutineLogIdOrderByCreatedAtDesc(routineLogId);

        if (existingChat.isPresent()) {
            AiChat chat = existingChat.get();
            List<AiChatMessage> messages = aiChatMessageRepository
                    .findByAiChatIdOrderByCreatedAtAsc(chat.getId());
            List<ChatMessageResponse> messageResponses = messages.stream()
                    .map(ChatMessageResponse::new)
                    .collect(Collectors.toList());
            return new ChatStartResponse(chat.getId(), routineLogId, false, messageResponses);
        }

        // 새 대화 생성
        AiChat newChat = AiChat.builder()
                .routineLog(routineLog)
                .build();
        aiChatRepository.save(newChat);

        // AI 첫 인사 메시지 저장
        AiChatMessage firstMessage = AiChatMessage.builder()
                .aiChat(newChat)
                .role("AI")
                .content("안녕하세요! 무엇을 도와드릴까요?")
                .causeTag(null)
                .build();
        aiChatMessageRepository.save(firstMessage);

        return new ChatStartResponse(
                newChat.getId(),
                routineLogId,
                true,
                List.of(new ChatMessageResponse(firstMessage))
        );
    }

    // 2. 원인 답변 전송
    @Transactional
    public ChatMessageResponse sendMessage(Long chatId, ChatMessageRequest request) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        AiChatMessage message = AiChatMessage.builder()
                .aiChat(chat)
                .role("USER")
                .content(request.getContent())
                .causeTag(request.getCauseTag())
                .build();

        aiChatMessageRepository.save(message);
        return new ChatMessageResponse(message);
    }

    // 3. 대체 미션 생성 (더미 데이터)
    @Transactional
    public MissionGenerateResponse generateMission(Long chatId) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 이전 PENDING 미션 REJECTED 처리
        Optional<AlternativeMission> pendingMission = alternativeMissionRepository
                .findByAiChatIdAndStatus(chatId, "PENDING");

        MissionResponse previousMission = null;
        if (pendingMission.isPresent()) {
            pendingMission.get().reject();
            previousMission = new MissionResponse(pendingMission.get());
        }

        // 대화에서 원인 태그 가져오기
        List<AiChatMessage> messages = aiChatMessageRepository
                .findByAiChatIdOrderByCreatedAtAsc(chatId);
        String causeTag = messages.stream()
                .filter(m -> m.getRole().equals("USER"))
                .map(AiChatMessage::getCauseTag)
                .findFirst()
                .orElse("기타");

        // 더미 데이터로 미션 생성 (나중에 GPT API로 교체)
        Map<String, Object> missionData = generateDummyMission(causeTag);

        AlternativeMission newMission = AlternativeMission.builder()
                .aiChat(chat)
                .content((String) missionData.get("content"))
                .durationMinutes((Integer) missionData.get("durationMinutes"))
                .difficulty((String) missionData.get("difficulty"))
                .build();
        alternativeMissionRepository.save(newMission);

        return new MissionGenerateResponse(new MissionResponse(newMission), previousMission);
    }

    // 4. 대체 미션 수락/거절
    @Transactional
    public MissionActionResponse handleMissionAction(Long missionId, MissionActionRequest request) {
        AlternativeMission mission = alternativeMissionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!mission.getStatus().equals("PENDING")) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_MISSION);
        }

        String routineLogStatus;

        if (request.getAction().equals("ACCEPT")) {
            mission.accept();
            routineLogStatus = "대체미션진행중";
        } else if (request.getAction().equals("REJECT")) {
            mission.reject();
            routineLogStatus = "미완료";
        } else {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        return new MissionActionResponse(missionId, mission.getStatus(), routineLogStatus);
    }

    // 더미 미션 생성 (GPT API 연동 전까지 사용)
    private Map<String, Object> generateDummyMission(String causeTag) {
        return switch (causeTag) {
            case "약속" -> Map.of(
                    "content", "약속 장소까지 걸어가기 + 단백질 쉐이크 한 잔",
                    "durationMinutes", 15,
                    "difficulty", "쉬움"
            );
            case "피로" -> Map.of(
                    "content", "가벼운 스트레칭 5분",
                    "durationMinutes", 5,
                    "difficulty", "쉬움"
            );
            case "시간부족" -> Map.of(
                    "content", "10분 압축 운동 (스쿼트 20개 + 팔굽혀펴기 10개)",
                    "durationMinutes", 10,
                    "difficulty", "보통"
            );
            case "기분" -> Map.of(
                    "content", "좋아하는 음악 들으며 5분 산책",
                    "durationMinutes", 5,
                    "difficulty", "쉬움"
            );
            default -> Map.of(
                    "content", "오늘 운동 대신 가벼운 스트레칭 10분 어떠세요?",
                    "durationMinutes", 10,
                    "difficulty", "쉬움"
            );
        };
    }
}
