package com.likelion.team4.domain.user.service;

import com.likelion.team4.domain.user.dto.request.UpdateNicknameRequest;
import com.likelion.team4.domain.user.dto.response.UserProfileResponse;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final UserRepository userRepository;

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

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}