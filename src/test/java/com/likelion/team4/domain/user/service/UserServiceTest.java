package com.likelion.team4.domain.user.service;

import com.likelion.team4.domain.user.dto.request.SignupRequest;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import com.likelion.team4.global.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest() {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "loginId", "tester01");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "nickname", "테스터");
        return request;
    }

    @Test
    void 아이디가_이미_존재하면_회원가입시_예외를_던진다() {
        SignupRequest request = signupRequest();
        when(userRepository.existsByLoginId(request.getLoginId())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    void 동시_요청으로_중복확인을_통과해도_DB_유니크_제약_위반시_중복_아이디_예외로_변환한다() {
        SignupRequest request = signupRequest();
        when(userRepository.existsByLoginId(request.getLoginId())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    void 정상_회원가입시_저장된_유저_정보로_응답을_반환한다() {
        SignupRequest request = signupRequest();
        when(userRepository.existsByLoginId(request.getLoginId())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.builder()
                    .id(1L)
                    .loginId(user.getLoginId())
                    .password(user.getPassword())
                    .nickname(user.getNickname())
                    .status(user.getStatus())
                    .routineAlarmOn(user.isRoutineAlarmOn())
                    .altMissionReminderOn(user.isAltMissionReminderOn())
                    .alarmSound(user.getAlarmSound())
                    .alarmOffsetType(user.getAlarmOffsetType())
                    .currentStreak(user.getCurrentStreak())
                    .maxStreak(user.getMaxStreak())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        });

        var response = userService.signup(request);

        assertThat(response.getNickname()).isEqualTo("테스터");
    }
}
