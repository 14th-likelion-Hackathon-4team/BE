package com.likelion.team4.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.team4.domain.chat.dto.request.ChatMessageRequest;
import com.likelion.team4.domain.chat.dto.request.MissionActionRequest;
import com.likelion.team4.domain.chat.dto.response.*;
import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.entity.AiChatMessage;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.entity.enums.MessageRole;
import com.likelion.team4.domain.chat.entity.enums.MissionStatus;
import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.routine.dto.response.AlternativeMissionCompleteResponse;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.service.RoutineLogProvisioner;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final String INITIAL_ROUTINE_LOG_STATUS = "미완료";
    private static final int DEFAULT_DURATION_MINUTES = 15;
    private static final int MIN_DURATION_MINUTES = 5;
    private static final int MAX_DURATION_MINUTES = 120;

    private final AiChatRepository aiChatRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final AlternativeMissionRepository alternativeMissionRepository;
    private final RoutineRepository routineRepository;
    private final RoutineLogProvisioner routineLogProvisioner;
    private final AiChatProvisioner aiChatProvisioner;
    private final MissionGenerationHelper missionGenerationHelper;
    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper;

    // 1. 대화 시작 (routineId 기준 - 오늘자 RoutineLog가 없으면 이 시점에 생성)
    @Transactional
    public ChatStartResponse startChat(Long routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        RoutineLog routineLog = getOrCreateTodayRoutineLog(routine);
        Long routineLogId = routineLog.getId();

        Optional<AiChat> existingChat = aiChatProvisioner.findExisting(routineLogId);

        if (existingChat.isPresent()) {
            return buildContinuedChatResponse(existingChat.get(), routineLogId);
        }

        AiChat newChat;
        try {
            newChat = aiChatProvisioner.create(routineLog);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 다른 트랜잭션이 먼저 대화를 시작한 경우 - 그 대화를 이어감
            AiChat chat = aiChatProvisioner.findExisting(routineLogId).orElseThrow(() -> e);
            return buildContinuedChatResponse(chat, routineLogId);
        }

        AiChatMessage firstMessage = AiChatMessage.builder()
                .aiChat(newChat)
                .role(MessageRole.AI)
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

    private ChatStartResponse buildContinuedChatResponse(AiChat chat, Long routineLogId) {
        List<AiChatMessage> messages = aiChatMessageRepository
                .findByAiChatIdOrderByCreatedAtAsc(chat.getId());
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(ChatMessageResponse::new)
                .collect(Collectors.toList());
        return new ChatStartResponse(chat.getId(), routineLogId, false, messageResponses);
    }

    private RoutineLog getOrCreateTodayRoutineLog(Routine routine) {
        LocalDate today = LocalDate.now(SEOUL_ZONE);

        Optional<RoutineLog> existing = routineLogProvisioner.find(routine.getId(), today);
        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            return routineLogProvisioner.create(routine, today);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 다른 트랜잭션이 먼저 생성한 경우 - 별도 트랜잭션으로 재조회
            return routineLogProvisioner.find(routine.getId(), today)
                    .orElseThrow(() -> e);
        }
    }

    // 2. 원인 답변 전송
    @Transactional
    public ChatMessageResponse sendMessage(Long chatId, ChatMessageRequest request) {
        AiChat chat = aiChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        AiChatMessage message = AiChatMessage.builder()
                .aiChat(chat)
                .role(MessageRole.USER)
                .content(request.getContent())
                .causeTag(request.getCauseTag())
                .build();

        aiChatMessageRepository.save(message);
        return new ChatMessageResponse(message);
    }

    // 3. 대체 미션 생성 (GPT API 호출)
    // 트랜잭션은 DB 준비 단계(prepare)와 저장 단계(saveMission)에만 짧게 걸림.
    // GPT 호출(최대 10초 블로킹) 동안은 DB 커넥션을 붙잡지 않음.
    public MissionGenerateResponse generateMission(Long chatId) {
        PreparedMissionContext context = missionGenerationHelper.prepare(chatId);

        Map<String, Object> missionData = callGptApi(
                context.routineTitle(), context.causeTag(), context.userReason()
        );

        MissionSaveResult result = missionGenerationHelper.saveMission(
                chatId, context.pendingMissionId(), missionData, context.missionDate()
        );

        return new MissionGenerateResponse(
                new MissionResponse(result.newMission()),
                result.previousMission()
        );
    }

    // 4. 대체 미션 수락/거절
    @Transactional
    public MissionActionResponse handleMissionAction(Long missionId, MissionActionRequest request) {
        AlternativeMission mission = alternativeMissionRepository.findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (mission.getStatus() != MissionStatus.PENDING) {
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

        try {
            // 낙관적 락 충돌은 커밋 시점에 발생하므로, 여기서 강제로 flush해서 그 자리에서 잡음
            alternativeMissionRepository.saveAndFlush(mission);
        } catch (ObjectOptimisticLockingFailureException e) {
            // 동시 요청으로 다른 트랜잭션이 먼저 처리한 경우
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_MISSION);
        }

        mission.getAiChat().getRoutineLog().updateStatus(routineLogStatus);

        return new MissionActionResponse(missionId, mission.getStatus().name(), routineLogStatus);
    }

    //5.대체미션 클리어
    @Transactional
    public AlternativeMissionCompleteResponse completeMission(Long missionId) {

        AlternativeMission mission =
                alternativeMissionRepository.findById(missionId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.ALTERNATIVE_MISSION_NOT_FOUND
                                ));

        mission.complete();

        return AlternativeMissionCompleteResponse.builder()
                .missionId(missionId)
                .alternativeMissionCompleted(true)
                .completedAt(LocalDateTime.now())
                .build();
    }

    // GPT API 호출
    private Map<String, Object> callGptApi(String routineTitle, String causeTag, String userReason) {
        String reasonText = (userReason == null || userReason.isBlank()) ? "(별도 설명 없음)" : userReason;

        String prompt = String.format(
                "사용자의 원래 루틴: %s\n" +
                        "오늘 이 루틴을 못 지킨 이유(분류): %s\n" +
                        "사용자가 직접 설명한 상황: %s\n\n" +
                        "위 상황에 맞는 대체 미션을 제안해주세요.\n\n" +
                        "조건:\n" +
                        "1. 대체 미션은 '이유'를 없애거나 해결하는 것이 아니라, 원래 루틴(\"%s\")의 목표와 " +
                        "어느 정도 연결되어야 합니다.\n" +
                        "2. 너무 쉽게 끝낼 수 있는 수준으로 낮추지 말고, 원래 루틴보다는 부담이 적지만 " +
                        "의미 있는 수준으로 제안하세요.\n" +
                        "3. 원래 루틴이 '하루 물 2L 마시기'처럼 하루 전체에 걸쳐 수행하는 습관이어도, " +
                        "대체 미션은 지금 당장 짧게 실행할 수 있는 구체적인 행동 하나여야 합니다 " +
                        "(예: 하루 물 2L 마시기 → 지금 물 한 컵 마시기).\n" +
                        "4. durationMinutes는 그 대체 미션 하나를 실제로 수행하는 데 걸리는 시간만 의미하며, " +
                        "5~120 사이의 현실적인 숫자여야 하고 보통은 60분 이내로 제안하세요. " +
                        "하루 종일/여러 시간에 걸친 시간을 넣지 마세요.\n" +
                        "5. 아래는 스타일 참고용 예시입니다 (그대로 복사하지 말고, 이번 상황에 맞게 새로 생성하세요):\n" +
                        "   - 오늘 운동하기 / 갑자기 약속이 잡힘 → 단백질 쉐이크 1잔 마시고 약속 장소까지 걸어가기\n" +
                        "   - 헬스장 가기 / 피로가 너무 심함 → 집에서 가볍게 맨몸운동 하기\n" +
                        "   - 책 30분 읽기 / 집중이 안 됨 → 책상 정리 10분 하기\n" +
                        "   - 하루 만보 걷기 / 비가 옴 → 집에서 15분 움직이기\n" +
                        "   - 하루 물 2L 마시기 / 물을 자주 못 마심 → 지금 물 한 컵 마시기\n\n" +
                        "반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 절대 포함하지 마세요:\n" +
                        "{\"content\": \"미션 내용\", \"durationMinutes\": 5~120 사이의 숫자, \"difficulty\": \"쉬움 또는 보통 또는 어려움\"}",
                routineTitle, causeTag, reasonText, routineTitle
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "max_tokens", 500,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            Map response = openAiWebClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(java.time.Duration.ofSeconds(10));

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String text = (String) message.get("content");

            // JSON 코드블록 제거 후 파싱
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();

            Map<String, Object> missionData = objectMapper.readValue(text, Map.class);

            // GPT가 프롬프트 지시(5~120분, 숫자 타입)를 안 지킬 수 있으므로 서버에서 한 번 더 안전하게 처리
            missionData.put("durationMinutes", resolveDurationMinutes(missionData.get("durationMinutes")));

            return missionData;

        } catch (WebClientResponseException e) {
            log.error(
                    "GPT API 호출 실패 (OpenAI 응답 오류) status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e
            );
            throw new CustomException(ErrorCode.LLM_TIMEOUT);
        } catch (WebClientRequestException e) {
            log.error("GPT API 호출 실패 (네트워크/연결 오류)", e);
            throw new CustomException(ErrorCode.LLM_TIMEOUT);
        } catch (Exception e) {
            log.error("GPT API 호출 실패 (응답 파싱 등 예상치 못한 오류)", e);
            throw new CustomException(ErrorCode.LLM_TIMEOUT);
        }
    }

    // GPT가 durationMinutes를 숫자가 아닌 타입(문자열 등)으로 주거나 범위를 벗어나게 줄 수 있으므로
    // 항상 유효한 Integer(5~120)로 정규화한다. 파싱 자체가 불가능하면 기본값을 사용한다.
    private int resolveDurationMinutes(Object durationRaw) {
        Integer parsed = null;

        if (durationRaw instanceof Number number) {
            parsed = number.intValue();
        } else if (durationRaw instanceof String text) {
            String digitsOnly = text.replaceAll("[^0-9]", "");
            if (!digitsOnly.isEmpty()) {
                try {
                    parsed = Integer.parseInt(digitsOnly);
                } catch (NumberFormatException ignored) {
                    // 아래에서 기본값 처리
                }
            }
        }

        if (parsed == null) {
            log.warn("GPT 응답의 durationMinutes를 파싱할 수 없어 기본값({}) 사용: raw={}",
                    DEFAULT_DURATION_MINUTES, durationRaw);
            return DEFAULT_DURATION_MINUTES;
        }

        return Math.max(MIN_DURATION_MINUTES, Math.min(MAX_DURATION_MINUTES, parsed));
    }
}