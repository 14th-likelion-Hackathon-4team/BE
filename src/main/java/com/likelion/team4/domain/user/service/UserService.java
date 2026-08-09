package com.likelion.team4.domain.user.service;

import com.likelion.team4.domain.user.dto.request.LoginRequest;
import com.likelion.team4.domain.user.dto.request.SignupRequest;
import com.likelion.team4.domain.user.dto.response.LoginResponse;
import com.likelion.team4.domain.user.dto.response.SignupResponse;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import com.likelion.team4.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 아이디 중복 확인
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 비밀번호 해시화 후 저장
        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        User savedUser = userRepository.save(user);
        return new SignupResponse(savedUser);
    }

    // 아이디 중복 확인
    public boolean checkLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 아이디로 회원 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        // 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Refresh Token DB 저장
        user.updateRefreshToken(refreshToken, jwtUtil.getRefreshTokenExpiresAt());

        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getNickname());
    }

    // 토큰 재발급
    @Transactional
    public String reissue(String refreshToken) {
        // Refresh Token으로 회원 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN));

        // Refresh Token 만료 확인
        if (user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // 새 Access Token 발급
        return jwtUtil.generateAccessToken(user.getId());
    }
}
