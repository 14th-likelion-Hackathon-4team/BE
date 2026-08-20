package com.likelion.team4.domain.user.service;

import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import com.likelion.team4.domain.user.dto.request.DeleteRequest;
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

    // 하위 도메인 삭제를 위한 의존성 주입
    private final AlternativeMissionRepository alternativeMissionRepository;
    private final AiChatMessageRepository aiChatMessageRepository;
    private final AiChatRepository aiChatRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineRecordRepository routineRecordRepository;
    private final RoutineRepository routineRepository;
    private final NotificationRepository notificationRepository;

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

        // 4. 비밀번호가 변경되었으므로 기존 로그인 세션(Refresh Token) 무효화
        user.updateRefreshToken(null, null);
    }

    // 알림 설정 변경
    @Transactional
    public UserProfileResponse updateAlarmSettings(Long userId, UpdateAlarmRequest request) {
        User user = getUserById(userId);

        // 프론트가 보낸 값을 한 번 더 정제
        Integer finalOffsetMinutes = request.getAlarmOffsetMinutes();

        // '직접설정'일 때 분(minutes) 값이 누락되었는지 검증
        if ("직접설정".equals(request.getAlarmOffsetType()) && request.getAlarmOffsetMinutes() == null) {
            throw new CustomException(ErrorCode.INVALID_ALARM_OFFSET); // 예: "직접설정 시 알림 시간을 입력해야 합니다."를 나타내는 에러 코드
        }

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

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId, DeleteRequest request) {
        User user = getUserById(userId);

        // 1. 비밀번호 검증 (입력받은 비밀번호와 DB의 암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 불일치 시 400 Bad Request 예외 발생
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 2. 연관된 하위 데이터 Cascade 물리적 삭제
        deleteChatDomains(userId);
        deleteRoutineDomains(userId);
        deleteNotificationDomains(userId);

        // 3. 부모(유저) 계정 탈퇴 처리 (물리적 삭제)
        // 레코드가 삭제되면서 내부의 리프레시 토큰 정보와 개인정보가 완전 파기됨
        userRepository.delete(user);
    }

    private void deleteChatDomains(Long userId) {
        // Chat 도메인 자식 엔티티부터 일괄 삭제
        alternativeMissionRepository.deleteAllByAiChat_RoutineLog_Routine_User_Id(userId);
        aiChatMessageRepository.deleteAllByAiChat_RoutineLog_Routine_User_Id(userId);
        aiChatRepository.deleteAllByRoutineLog_Routine_User_Id(userId);
    }

    private void deleteRoutineDomains(Long userId) {
        // Routine 도메인 자식 엔티티 일괄 삭제
        routineLogRepository.deleteAllByRoutine_User_Id(userId);
        routineRecordRepository.deleteAllByRoutine_User_Id(userId);
        routineRepository.deleteAllByUser_Id(userId);
    }

    private void deleteNotificationDomains(Long userId) {
        // 알림 데이터 일괄 삭제
        notificationRepository.deleteAllByUser_Id(userId);
    }
}