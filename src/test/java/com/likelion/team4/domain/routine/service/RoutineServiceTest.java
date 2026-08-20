package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.dto.request.RoutineRequest;
import com.likelion.team4.domain.routine.dto.response.RoutineResponse;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.enums.RepeatType;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutineServiceTest {

    @Mock private RoutineRepository routineRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RoutineService routineService;

    private User user;
    private RoutineRequest baseRequest;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // 공통 더미 유저 생성
        user = User.builder()
                .id(userId)
                .build();

        // 공통 정상 요청 DTO 생성 (Weekly 기준)
        baseRequest = new RoutineRequest(
                "기본 루틴", LocalTime.of(8, 0), "MON,WED,FRI",
                true, true,
                LocalDate.now(), LocalDate.now().plusMonths(1),
                LocalTime.of(7, 50), RepeatType.WEEKLY, null
        );
    }

    // ---------- 루틴 생성 검증 ----------

    @Test
    @DisplayName("루틴 생성 - 성공 (WEEKLY 타입)")
    void createRoutine_success() {
        // given
        Routine dummyRoutine = Routine.builder()
                .user(user).title(baseRequest.getTitle())
                .repeatType(RepeatType.WEEKLY).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(routineRepository.save(any(Routine.class))).willReturn(dummyRoutine);

        // when
        RoutineResponse response = routineService.createRoutine(userId, baseRequest);

        // then
        assertThat(response.getTitle()).isEqualTo("기본 루틴");
        verify(routineRepository).save(any(Routine.class));
    }

    @Test
    @DisplayName("루틴 생성 - 종료일이 시작일보다 빠르면 예외 발생")
    void createRoutine_dateInversion_exception() {
        // given
        baseRequest.setStartDate(LocalDate.of(2026, 8, 20));
        baseRequest.setEndDate(LocalDate.of(2026, 8, 10)); // 역전된 날짜
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> routineService.createRoutine(userId, baseRequest))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("루틴 생성 - COUNT 타입인데 알람이 설정되어 있으면 예외 발생")
    void createRoutine_countTypeWithAlarm_exception() {
        // given
        baseRequest.setRepeatType(RepeatType.COUNT);
        baseRequest.setRepeatCount(3);
        baseRequest.setAlarm(true); // COUNT 타입인데 알람 활성화
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> routineService.createRoutine(userId, baseRequest))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    // ---------- 루틴 상세 조회 및 권한 검증 ----------

    @Test
    @DisplayName("루틴 상세 조회 - 타인의 루틴에 접근 시 FORBIDDEN 예외 발생")
    void getRoutine_forbiddenAccess() {
        // given
        Long routineId = 100L;
        Long anotherUserId = 999L; // 요청자와 다른 ID

        User anotherUser = User.builder().id(anotherUserId).build();
        Routine othersRoutine = Routine.builder().user(anotherUser).build();

        given(routineRepository.findById(routineId)).willReturn(Optional.of(othersRoutine));

        // when & then
        assertThatThrownBy(() -> routineService.getRoutine(userId, routineId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
    }

    // ---------- 루틴 목록 조회 검증 ----------

    @Test
    @DisplayName("전체 루틴 조회 - 성공")
    void getAllRoutines_success() {
        // given
        Routine routine1 = Routine.builder().user(user).title("루틴1").build();
        Routine routine2 = Routine.builder().user(user).title("루틴2").build();

        given(routineRepository.findAllByUser_IdAndDeletedAtIsNull(userId))
                .willReturn(List.of(routine1, routine2));

        // when
        List<RoutineResponse> responses = routineService.getAllRoutines(userId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("루틴1");
    }

    @Test
    @DisplayName("특정 요일 루틴 조회 - 성공")
    void getRoutinesByDay_success() {
        // given
        String day = "MON";
        Routine routine1 = Routine.builder().user(user).title("월요 루틴").repeatDays("MON,WED").build();

        given(routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(userId, day))
                .willReturn(List.of(routine1));

        // when
        List<RoutineResponse> responses = routineService.getRoutinesByDay(userId, day);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("월요 루틴");
    }

    // ---------- 루틴 수정 검증 ----------

    @Test
    @DisplayName("루틴 수정 - 성공")
    void updateRoutine_success() {
        // given
        Long routineId = 1L;
        Routine existingRoutine = Routine.builder()
                .user(user).title("기존 루틴")
                .repeatType(RepeatType.WEEKLY).build();

        baseRequest.setTitle("수정된 루틴"); // baseRequest 재사용

        given(routineRepository.findById(routineId)).willReturn(Optional.of(existingRoutine));

        // when
        RoutineResponse response = routineService.updateRoutine(userId, routineId, baseRequest);

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 루틴");
        assertThat(existingRoutine.getTitle()).isEqualTo("수정된 루틴"); // 더티 체킹 확인
    }

    // ---------- 루틴 활성화/비활성화 검증 ----------

    @Test
    @DisplayName("루틴 상태 변경 - 성공")
    void updateActiveStatus_success() {
        // given
        Long routineId = 1L;
        Routine existingRoutine = Routine.builder()
                .user(user).title("상태 변경 루틴").active(true).build();

        given(routineRepository.findById(routineId)).willReturn(Optional.of(existingRoutine));

        // when
        RoutineResponse response = routineService.updateActiveStatus(userId, routineId, false);

        // then
        assertThat(response.isActive()).isFalse();
        assertThat(existingRoutine.isActive()).isFalse();
    }

    // ---------- 루틴 삭제 검증 ----------

    @Test
    @DisplayName("루틴 삭제 - 성공 시 deletedAt 필드가 업데이트된다 (소프트 삭제)")
    void deleteRoutine_success() {
        // given
        Long routineId = 1L;
        Routine existingRoutine = Routine.builder()
                .user(user).title("삭제할 루틴").build();

        given(routineRepository.findById(routineId)).willReturn(Optional.of(existingRoutine));

        // when
        routineService.deleteRoutine(userId, routineId);

        // then
        assertThat(existingRoutine.getDeletedAt()).isNotNull(); // 삭제 시간이 기록되었는지 검증
    }

    @Test
    @DisplayName("루틴 삭제 - 이미 삭제된 루틴 접근 시 NOT_FOUND 예외 발생")
    void deleteRoutine_alreadyDeleted_exception() {
        // given
        Long routineId = 1L;
        Routine deletedRoutine = Routine.builder().user(user).build();
        deletedRoutine.deleteSoftly(); // 사전에 삭제 처리

        given(routineRepository.findById(routineId)).willReturn(Optional.of(deletedRoutine));

        // when & then
        assertThatThrownBy(() -> routineService.deleteRoutine(userId, routineId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}