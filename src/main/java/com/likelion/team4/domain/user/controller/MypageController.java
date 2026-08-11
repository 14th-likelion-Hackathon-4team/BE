package com.likelion.team4.domain.user.controller;

import com.likelion.team4.domain.user.dto.request.UpdateAlarmRequest;
import com.likelion.team4.domain.user.dto.request.UpdateNicknameRequest;
import com.likelion.team4.domain.user.dto.request.UpdatePasswordRequest;
import com.likelion.team4.domain.user.dto.response.UserProfileResponse;
import com.likelion.team4.domain.user.service.MypageService;
import com.likelion.team4.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routinefit/users")
public class MypageController {
    private final MypageService mypageService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        UserProfileResponse response = mypageService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("S200", "내 정보 조회 성공", response));
    }

    // 닉네임 변경
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateNicknameRequest request) {
        UserProfileResponse response = mypageService.updateNickname(userId, request);
        return ResponseEntity.ok(ApiResponse.success("S200", "닉네임 변경 성공", response));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdatePasswordRequest request) {
        mypageService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("S200", "비밀번호 변경 성공", null));
    }

    // 알림 설정 변경
    @PatchMapping("/me/notifications")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAlarmSettings(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateAlarmRequest request) {
        UserProfileResponse response = mypageService.updateAlarmSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success("S200", "알림 설정 변경 성공", response));
    }
}
