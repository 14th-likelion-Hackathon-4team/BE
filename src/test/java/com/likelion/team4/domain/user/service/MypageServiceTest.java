package com.likelion.team4.domain.user.service;

import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.repository.RoutineAlternativeMissionRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MypageServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AlternativeMissionRepository alternativeMissionRepository;
    @Mock private AiChatMessageRepository aiChatMessageRepository;
    @Mock private AiChatRepository aiChatRepository;
    @Mock private RoutineLogRepository routineLogRepository;
    @Mock private RoutineRecordRepository routineRecordRepository;
    @Mock private RoutineAlternativeMissionRepository routineAlternativeMissionRepository;
    @Mock private RoutineRepository routineRepository;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private MypageService mypageService;

    private User user;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .loginId("testuser")
                .password("encodedOldPassword")
                .nickname("기존닉네임")
                .status("ACTIVE")
                .currentStreak(3)
                .maxStreak(5)
                .routineAlarmOn(true)
                .altMissionReminderOn(true)
                .alarmSound("차분한벨")
                .alarmOffsetType("1시간전")
                .alarmOffsetMinutes(null)
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    // ---------- 내 정보 조회 ----------

    @Test
    @DisplayName("내 정보 조회 - 성공")
    void getMyProfile_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = mypageService.getMyProfile(userId);

        assertThat(response.getNickname()).isEqualTo("기존닉네임");
        assertThat(response.getLoginId()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("내 정보 조회 - 존재하지 않는 유저면 예외")
    void getMyProfile_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mypageService.getMyProfile(userId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    // ---------- 닉네임 변경 ----------

    @Test
    @DisplayName("닉네임 변경 - 성공")
    void updateNickname_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdateNicknameRequest request = new UpdateNicknameRequest("새닉네임");

        UserProfileResponse response = mypageService.updateNickname(userId, request);

        assertThat(response.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    // ---------- 비밀번호 변경 ----------

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void updatePassword_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPw123", "newPw123!");

        when(passwordEncoder.matches("currentPw123", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPw123!", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPw123!")).thenReturn("encodedNewPassword");

        mypageService.updatePassword(userId, request);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호 불일치 시 예외")
    void updatePassword_wrongCurrentPassword() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdatePasswordRequest request = new UpdatePasswordRequest("wrongPw", "newPw123!");
        when(passwordEncoder.matches("wrongPw", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> mypageService.updatePassword(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("비밀번호 변경 - 새 비밀번호가 기존과 동일하면 예외")
    void updatePassword_sameAsOld() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdatePasswordRequest request = new UpdatePasswordRequest("currentPw123", "currentPw123");
        // 현재 비밀번호 검증과 "새 비밀번호=기존 비밀번호" 검증이 같은 문자열로 호출되므로
        // 하나의 stub으로 두 matches() 호출 모두 true를 반환합니다.
        when(passwordEncoder.matches("currentPw123", user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> mypageService.updatePassword(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SAME_AS_OLD_PASSWORD);
    }

    // ---------- 알림 설정 변경 ----------

    @Test
    @DisplayName("알림 설정 변경 - 성공(직접설정 + 분 값 포함)")
    void updateAlarmSettings_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdateAlarmRequest request = buildAlarmRequest(false, false, "신나는벨", "직접설정", 15);

        UserProfileResponse response = mypageService.updateAlarmSettings(userId, request);

        assertThat(response.getAlarmSound()).isEqualTo("신나는벨");
        assertThat(response.getAlarmOffsetType()).isEqualTo("직접설정");
        assertThat(response.getAlarmOffsetMinutes()).isEqualTo(15);
    }

    @Test
    @DisplayName("알림 설정 변경 - 직접설정인데 분 값이 없으면 예외")
    void updateAlarmSettings_customOffsetWithoutMinutes() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UpdateAlarmRequest request = buildAlarmRequest(true, true, "차분한벨", "직접설정", null);

        assertThatThrownBy(() -> mypageService.updateAlarmSettings(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_ALARM_OFFSET);

        verify(userRepository, times(1)).findById(userId);
    }

    // ---------- 회원 탈퇴 ----------

    @Test
    @DisplayName("회원 탈퇴 - 성공 시 하위 도메인과 유저가 모두 삭제된다")
    void deleteUser_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        DeleteRequest request = new DeleteRequest("currentPw123");
        when(passwordEncoder.matches("currentPw123", user.getPassword())).thenReturn(true);

        mypageService.deleteUser(userId, request);

        verify(alternativeMissionRepository).deleteAllByAiChat_RoutineLog_Routine_User_Id(userId);
        verify(aiChatMessageRepository).deleteAllByAiChat_RoutineLog_Routine_User_Id(userId);
        verify(aiChatRepository).deleteAllByRoutineLog_Routine_User_Id(userId);
        verify(routineLogRepository).deleteAllByRoutine_User_Id(userId);
        verify(routineRecordRepository).deleteAllByRoutine_User_Id(userId);
        verify(routineAlternativeMissionRepository).deleteAllByRoutine_User_Id(userId);
        verify(routineRepository).deleteAllByUser_Id(userId);
        verify(notificationRepository).deleteAllByUser_Id(userId);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("회원 탈퇴 - 비밀번호 불일치 시 예외, 하위 도메인은 삭제되지 않는다")
    void deleteUser_wrongPassword() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        DeleteRequest request = new DeleteRequest("wrongPw");
        when(passwordEncoder.matches("wrongPw", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> mypageService.deleteUser(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PASSWORD);

        verify(userRepository, never()).delete(any());
        verifyNoInteractions(routineRepository, aiChatRepository, notificationRepository);
    }

    // ---------- 헬퍼 ----------

    private UpdateAlarmRequest buildAlarmRequest(boolean routineAlarmOn, boolean altReminderOn,
                                                 String sound, String offsetType, Integer minutes) {
        // UpdateAlarmRequest는 세터가 없는 DTO라 리플렉션으로 값을 채웁니다.
        UpdateAlarmRequest request = new UpdateAlarmRequest();
        ReflectionTestUtils.setField(request, "routineAlarmOn", routineAlarmOn);
        ReflectionTestUtils.setField(request, "altMissionReminderOn", altReminderOn);
        ReflectionTestUtils.setField(request, "alarmSound", sound);
        ReflectionTestUtils.setField(request, "alarmOffsetType", offsetType);
        ReflectionTestUtils.setField(request, "alarmOffsetMinutes", minutes);
        return request;
    }
}