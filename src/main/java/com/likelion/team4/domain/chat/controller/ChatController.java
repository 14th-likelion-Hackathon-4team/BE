package com.likelion.team4.domain.chat.controller;

import com.likelion.team4.domain.chat.dto.request.ChatMessageRequest;
import com.likelion.team4.domain.chat.dto.request.MissionActionRequest;
import com.likelion.team4.domain.chat.dto.response.*;
import com.likelion.team4.domain.chat.service.ChatService;
import com.likelion.team4.domain.routine.dto.response.AlternativeMissionCompleteResponse;
import com.likelion.team4.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routinefit")
public class ChatController {

    private final ChatService chatService;

    // 1. 대화 시작
    @PostMapping("/routines/{routineId}/chats")
    public ResponseEntity<ApiResponse<ChatStartResponse>> startChat(
            @PathVariable Long routineId) {
        ChatStartResponse response = chatService.startChat(routineId);
        boolean isNew = response.isNew();
        if (isNew) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("S201", "대화 시작 성공", response));
        }
        return ResponseEntity.ok(ApiResponse.success("S200", "진행 중인 대화를 이어갑니다", response));
    }

    // 2. 원인 답변 전송
    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(chatId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("S201", "메시지 전송 성공", response));
    }

    // 3. 대체 미션 생성
    @PostMapping("/chats/{chatId}/missions")
    public ResponseEntity<ApiResponse<MissionGenerateResponse>> generateMission(
            @PathVariable Long chatId) {
        MissionGenerateResponse response = chatService.generateMission(chatId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("S201", "대체 미션 제안 성공", response));
    }

    // 4. 대체 미션 수락/거절
    @PatchMapping("/missions/{missionId}")
    public ResponseEntity<ApiResponse<MissionActionResponse>> handleMissionAction(
            @PathVariable Long missionId,
            @Valid @RequestBody MissionActionRequest request) {
        MissionActionResponse response = chatService.handleMissionAction(missionId, request);
        return ResponseEntity.ok(ApiResponse.success("S200",
                request.getAction().equals("ACCEPT") ? "대체 미션을 수락했습니다" : "대체 미션을 거절했습니다",
                response));
    }

    // 5. 대체 미션 완료
    @PatchMapping("/missions/{missionId}/complete")
    public ResponseEntity<ApiResponse<AlternativeMissionCompleteResponse>> completeMission(
            @PathVariable Long missionId) {

        AlternativeMissionCompleteResponse response =
                chatService.completeMission(missionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "대체 미션이 완료되었습니다.",
                        response
                )
        );
    }
}
