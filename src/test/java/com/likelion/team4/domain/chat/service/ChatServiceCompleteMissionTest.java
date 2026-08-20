package com.likelion.team4.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.entity.enums.MissionStatus;
import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.service.RoutineLogProvisioner;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * completeMission()이 ACCEPTED 상태가 아닌 미션은 완료 처리하지 않고,
 * 동시 완료 요청 시 낙관적 락 충돌을 정상적인 에러로 변환하는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceCompleteMissionTest {

    @Mock
    private AiChatRepository aiChatRepository;
    @Mock
    private AiChatMessageRepository aiChatMessageRepository;
    @Mock
    private AlternativeMissionRepository alternativeMissionRepository;
    @Mock
    private RoutineRepository routineRepository;
    @Mock
    private RoutineLogProvisioner routineLogProvisioner;
    @Mock
    private AiChatProvisioner aiChatProvisioner;
    @Mock
    private MissionGenerationHelper missionGenerationHelper;
    @Mock
    private WebClient openAiWebClient;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatService chatService;

    private static final Long OWNER_ID = 1L;
    private static final Long MISSION_ID = 40L;

    private AlternativeMission missionWithStatus(MissionStatus status) {
        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", OWNER_ID);

        Routine routine = Routine.builder().user(owner).title("아침운동").build();

        RoutineLog log = RoutineLog.builder()
                .routine(routine)
                .logDate(LocalDate.now())
                .status("대체미션진행중")
                .reminderSent(false)
                .build();

        AiChat chat = AiChat.builder().routineLog(log).build();

        AlternativeMission mission = AlternativeMission.builder()
                .aiChat(chat)
                .content("물 한 컵 마시기")
                .missionDate(LocalDate.now())
                .build();
        ReflectionTestUtils.setField(mission, "status", status);
        return mission;
    }

    @ParameterizedTest
    @EnumSource(value = MissionStatus.class, names = "ACCEPTED", mode = EnumSource.Mode.EXCLUDE)
    void ACCEPTED_상태가_아니면_완료처리를_거부한다(MissionStatus status) {
        AlternativeMission mission = missionWithStatus(status);
        when(alternativeMissionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));

        assertThatThrownBy(() -> chatService.completeMission(MISSION_ID, OWNER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_PROCESSED_MISSION);
    }

    @Test
    void ACCEPTED_상태면_정상적으로_완료처리된다() {
        AlternativeMission mission = missionWithStatus(MissionStatus.ACCEPTED);
        when(alternativeMissionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));
        when(alternativeMissionRepository.saveAndFlush(mission)).thenReturn(mission);

        var response = chatService.completeMission(MISSION_ID, OWNER_ID);

        assertThat(response.isAlternativeMissionCompleted()).isTrue();
        assertThat(mission.getStatus()).isEqualTo(MissionStatus.COMPLETED);
    }

    @Test
    void 동시_완료_요청으로_낙관적_락_충돌이_나면_이미처리된미션_예외로_변환한다() {
        AlternativeMission mission = missionWithStatus(MissionStatus.ACCEPTED);
        when(alternativeMissionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));
        when(alternativeMissionRepository.saveAndFlush(mission))
                .thenThrow(new ObjectOptimisticLockingFailureException(AlternativeMission.class, MISSION_ID));

        assertThatThrownBy(() -> chatService.completeMission(MISSION_ID, OWNER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_PROCESSED_MISSION);
    }
}
