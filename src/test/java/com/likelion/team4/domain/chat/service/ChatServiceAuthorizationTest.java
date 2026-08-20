package com.likelion.team4.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.repository.AiChatMessageRepository;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.chat.dto.request.ChatMessageRequest;
import com.likelion.team4.domain.chat.dto.request.MissionActionRequest;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import com.likelion.team4.domain.routinelog.service.RoutineLogProvisioner;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * 다른 사용자의 채팅/미션 리소스에 접근할 수 없는지(IDOR 방지) 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceAuthorizationTest {

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
    private static final Long ATTACKER_ID = 2L;

    private User userWithId(Long id) {
        // User.builder()에 id 세터가 없으므로 리플렉션으로 직접 필드 주입
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Routine routineOwnedBy(Long ownerId) {
        Routine routine = Routine.builder()
                .user(userWithId(ownerId))
                .title("아침 운동")
                .build();
        return routine;
    }

    private RoutineLog routineLogFor(Routine routine) {
        RoutineLog log = RoutineLog.builder()
                .routine(routine)
                .logDate(java.time.LocalDate.now())
                .status("미완료")
                .reminderSent(false)
                .build();
        return log;
    }

    @Test
    void 다른_사용자의_루틴으로_대화를_시작하면_예외가_발생한다() {
        Routine othersRoutine = routineOwnedBy(OWNER_ID);
        when(routineRepository.findById(10L)).thenReturn(Optional.of(othersRoutine));

        assertThatThrownBy(() -> chatService.startChat(10L, ATTACKER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
    }

    @Test
    void 다른_사용자의_채팅에_메시지를_보내면_예외가_발생한다() {
        Routine othersRoutine = routineOwnedBy(OWNER_ID);
        RoutineLog log = routineLogFor(othersRoutine);
        AiChat chat = AiChat.builder().routineLog(log).build();
        when(aiChatRepository.findById(20L)).thenReturn(Optional.of(chat));

        ChatMessageRequest request = new ChatMessageRequest();

        assertThatThrownBy(() -> chatService.sendMessage(20L, request, ATTACKER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
    }

    @Test
    void 다른_사용자의_대체미션을_수락_거절하면_예외가_발생한다() {
        Routine othersRoutine = routineOwnedBy(OWNER_ID);
        RoutineLog log = routineLogFor(othersRoutine);
        AiChat chat = AiChat.builder().routineLog(log).build();
        AlternativeMission mission = AlternativeMission.builder()
                .aiChat(chat)
                .content("물 한 컵 마시기")
                .missionDate(java.time.LocalDate.now())
                .build();
        when(alternativeMissionRepository.findById(30L)).thenReturn(Optional.of(mission));

        MissionActionRequest request = new MissionActionRequest();
        ReflectionTestUtils.setField(request, "action", "ACCEPT");

        assertThatThrownBy(() -> chatService.handleMissionAction(30L, request, ATTACKER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
    }

    @Test
    void 다른_사용자의_대체미션을_완료처리하면_예외가_발생한다() {
        Routine othersRoutine = routineOwnedBy(OWNER_ID);
        RoutineLog log = routineLogFor(othersRoutine);
        AiChat chat = AiChat.builder().routineLog(log).build();
        AlternativeMission mission = AlternativeMission.builder()
                .aiChat(chat)
                .content("물 한 컵 마시기")
                .missionDate(java.time.LocalDate.now())
                .build();
        when(alternativeMissionRepository.findById(40L)).thenReturn(Optional.of(mission));

        assertThatThrownBy(() -> chatService.completeMission(40L, ATTACKER_ID))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN_ACCESS);
    }
}
