package com.likelion.team4.domain.main.service;

import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.entity.enums.MissionStatus;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.main.dto.MainResponse;
import com.likelion.team4.domain.main.dto.TodayRoutineResponse;
import com.likelion.team4.domain.main.repository.NotificationRepository;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.repository.RoutineLogRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 대체미션이 완료된 상태일 때 메인페이지 응답의 alternativeMissionCompleted가
 * 실제로 true를 반환하는지 검증한다. (enum vs String 비교로 항상 false만 나오던 버그)
 */
@ExtendWith(MockitoExtension.class)
class MainServiceTest {

    @Mock
    private RoutineRepository routineRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private RoutineRecordRepository routineRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoutineLogRepository routineLogRepository;
    @Mock
    private AlternativeMissionRepository alternativeMissionRepository;

    @InjectMocks
    private MainService mainService;

    private static final Long USER_ID = 1L;
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @Test
    void 대체미션이_완료_상태면_alternativeMissionCompleted가_true다() {
        User user = User.builder().nickname("테스터").build();
        ReflectionTestUtils.setField(user, "id", USER_ID);

        Routine routine = Routine.builder().user(user).title("아침운동").build();
        ReflectionTestUtils.setField(routine, "id", 100L);

        LocalDate today = LocalDate.now(SEOUL_ZONE);

        RoutineRecord routineRecord = RoutineRecord.builder()
                .routine(routine)
                .recordDate(today)
                .status(RoutineRecordStatus.PENDING)
                .build();

        RoutineLog routineLog = RoutineLog.builder()
                .routine(routine)
                .logDate(today)
                .status("대체미션진행중")
                .reminderSent(false)
                .build();
        AiChat aiChat = AiChat.builder().routineLog(routineLog).build();

        AlternativeMission mission = AlternativeMission.builder()
                .aiChat(aiChat)
                .content("물 한 컵 마시기")
                .missionDate(today)
                .build();
        ReflectionTestUtils.setField(mission, "status", MissionStatus.COMPLETED);
        ReflectionTestUtils.setField(mission, "completedAt", LocalDateTime.now());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(any(), any()))
                .thenReturn(List.of(routine));
        when(routineRecordRepository.findByRoutine_IdAndRecordDate(anyLong(), any()))
                .thenReturn(Optional.of(routineRecord));
        when(alternativeMissionRepository
                .findTopByAiChat_RoutineLog_Routine_IdAndMissionDateOrderByCreatedAtDesc(anyLong(), any()))
                .thenReturn(Optional.of(mission));

        MainResponse response = mainService.getMainPage(USER_ID);

        TodayRoutineResponse todayRoutine = response.getTodayRoutines().get(0);
        assertThat(todayRoutine.getAlternativeMissionStatus()).isEqualTo("COMPLETED");
        assertThat(todayRoutine.getAlternativeMissionCompleted()).isTrue();
        assertThat(todayRoutine.getRoutineStatus()).isEqualTo("ALTERNATIVE_COMPLETED");
    }
}
