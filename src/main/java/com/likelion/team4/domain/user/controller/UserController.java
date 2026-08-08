package com.likelion.team4.domain.user.controller;

import com.likelion.team4.domain.user.dto.request.LoginRequest;
import com.likelion.team4.domain.user.dto.request.SignupRequest;
import com.likelion.team4.domain.user.dto.response.LoginResponse;
import com.likelion.team4.domain.user.dto.response.SignupResponse;
import com.likelion.team4.domain.user.service.UserService;
import com.likelion.team4.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/routinefit/auth")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("S201", "회원가입 성공", response));
    }

    // 아이디 중복 확인
    @GetMapping("/check-id")
    public ResponseEntity<ApiResponse<Object>> checkId(
            @RequestParam String loginId) {
        boolean available = userService.checkLoginIdAvailable(loginId);
        String message = available ? "사용 가능한 아이디입니다" : "이미 사용 중인 아이디입니다";
        return ResponseEntity.ok(ApiResponse.success("S200", message,
                java.util.Map.of("available", available)));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("S200", "로그인 성공", response));
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Object>> reissue(
            @RequestBody java.util.Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = userService.reissue(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("S200", "토큰 재발급 성공",
                java.util.Map.of("accessToken", newAccessToken)));
    }
}
