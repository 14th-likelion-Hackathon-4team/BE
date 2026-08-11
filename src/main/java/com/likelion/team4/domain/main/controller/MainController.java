package com.likelion.team4.domain.main.controller;

import com.likelion.team4.domain.main.dto.*;
import com.likelion.team4.domain.main.service.MainService;
import com.likelion.team4.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routinefit/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    // 메인페이지 조회
    @GetMapping
    public ResponseEntity<ApiResponse<MainResponse>> getMainPage(
            @RequestParam Long userId
    ) {
        MainResponse response = mainService.getMainPage(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "메인 페이지 정보 조회 완료",
                        response
                )
        );
    }

    // 오늘 알림 목록 조회
    @GetMapping("/notifications/today")
    public ResponseEntity<ApiResponse<TodayNotificationListResponse>> getTodayNotifications(
            @RequestParam Long userId
    ) {
        TodayNotificationListResponse response =
                mainService.getTodayNotifications(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "오늘 알림 목록 조회 성공",
                        response
                )
        );
    }

    // 알림 읽음 처리
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> readNotification(
            @PathVariable Long notificationId
    ) {
        NotificationReadResponse response =
                mainService.readNotification(notificationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "S200",
                        "알림을 읽음 처리했습니다.",
                        response
                )
        );
    }
}