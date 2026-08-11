package com.likelion.team4.domain.user.service;

import com.likelion.team4.domain.user.dto.request.UpdateAlarmRequest;
import com.likelion.team4.domain.user.dto.request.UpdateNicknameRequest;
import com.likelion.team4.domain.user.dto.request.UpdatePasswordRequest;
import com.likelion.team4.domain.user.dto.response.UserProfileResponse;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = getUserById(userId);
        return new UserProfileResponse(user);
    }

    // 닉네임 변경
    @Transactional
    public UserProfileResponse updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = getUserById(userId);
        user.updateNickname(request.getNickname());
        return new UserProfileResponse(user);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = getUserById(userId);

        // 1. 현재 비밀번호 일치 여부 검증 (불일치 시 400 CustomException)
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 2. 새 비밀번호가 현재 비밀번호와 동일한지 검증 (일치 시 400 CustomException)
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }

        // 3. 새 비밀번호 암호화 후 반영
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedPassword);
    }

    // 알림 설정 변경
    @Transactional
    public UserProfileResponse updateAlarmSettings(Long userId, UpdateAlarmRequest request) {
        User user = getUserById(userId);

        user.updateAlarmSettings(
                request.getRoutineAlarmOn(),
                request.getAltMissionReminderOn(),
                request.getAlarmSound(),
                request.getAlarmOffsetType(),
                request.getAlarmOffsetMinutes()
        );

        return new UserProfileResponse(user);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}