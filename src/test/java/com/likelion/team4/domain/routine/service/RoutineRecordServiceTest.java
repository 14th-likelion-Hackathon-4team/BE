package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.report.service.StreakService;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 대체미션 완료 시 원래 루틴의 RoutineRecord도 함께 완료 처리되는지,
 * 그리고 동시 요청(중복 생성/중복 완료)이 안전하게 처리되는지 검증한다.
 * (이게 안 되면 자정 스케줄러가 PENDING을 그대로 INCOMPLETE로 덮어써버리는 버그로 이어짐)
 */
@ExtendWith(MockitoExtension.class)
class RoutineRecordServiceTest {

    @Mock
    private RoutineRepository routineRepository;
    @Mock
    private RoutineRecordProvisioner routineRecordProvisioner;
    @Mock
    private StreakService streakService;

    @InjectMocks
    private RoutineRecordService routineRecordService;

    private static final Long ROUTINE_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 21);

    private Routine routineWithOwner() {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        Routine routine = Routine.builder().user(user).title("아침운동").build();
        ReflectionTestUtils.setField(routine, "id", ROUTINE_ID);
        return routine;
    }

    /** provisioner.complete()가 실제로 record.complete()를 호출하는 것처럼 동작시킨다. */
    private void stubCompleteToActuallyComplete() {
        doAnswer(invocation -> {
            RoutineRecord record = invocation.getArgument(0);
            record.complete();
            return null;
        }).when(routineRecordProvisioner).complete(any());
    }

    @Test
    void 오늘자_레코드가_없으면_새로_만들어서_완료처리하고_스트릭을_갱신한다() {
        Routine routine = routineWithOwner();
        RoutineRecord created = RoutineRecord.builder()
                .routine(routine)
                .recordDate(TODAY)
                .status(RoutineRecordStatus.PENDING)
                .build();
        stubCompleteToActuallyComplete();

        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
        when(routineRecordProvisioner.find(ROUTINE_ID, TODAY)).thenReturn(Optional.empty());
        when(routineRecordProvisioner.create(routine, TODAY)).thenReturn(created);

        routineRecordService.completeRoutineByAlternativeMission(ROUTINE_ID, TODAY);

        assertThat(created.getStatus()).isEqualTo(RoutineRecordStatus.COMPLETED);
        verify(routineRecordProvisioner).complete(created);
        verify(streakService).updateStreak(USER_ID, ROUTINE_ID, TODAY);
    }

    @Test
    void PENDING_상태의_기존_레코드를_완료처리한다() {
        Routine routine = routineWithOwner();
        RoutineRecord pending = RoutineRecord.builder()
                .routine(routine)
                .recordDate(TODAY)
                .status(RoutineRecordStatus.PENDING)
                .build();
        stubCompleteToActuallyComplete();

        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
        when(routineRecordProvisioner.find(ROUTINE_ID, TODAY)).thenReturn(Optional.of(pending));

        routineRecordService.completeRoutineByAlternativeMission(ROUTINE_ID, TODAY);

        assertThat(pending.getStatus()).isEqualTo(RoutineRecordStatus.COMPLETED);
        verify(streakService).updateStreak(USER_ID, ROUTINE_ID, TODAY);
    }

    @Test
    void 이미_완료된_레코드면_아무것도_하지_않는다() {
        Routine routine = routineWithOwner();
        RoutineRecord completed = RoutineRecord.builder()
                .routine(routine)
                .recordDate(TODAY)
                .status(RoutineRecordStatus.COMPLETED)
                .build();

        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
        when(routineRecordProvisioner.find(ROUTINE_ID, TODAY)).thenReturn(Optional.of(completed));

        routineRecordService.completeRoutineByAlternativeMission(ROUTINE_ID, TODAY);

        verify(routineRecordProvisioner, never()).complete(any());
        verify(streakService, never()).updateStreak(anyLong(), anyLong(), any());
    }

    @Test
    void 동시_요청으로_생성이_충돌하면_재조회해서_기존_레코드를_사용한다() {
        Routine routine = routineWithOwner();
        RoutineRecord raceWinnerRecord = RoutineRecord.builder()
                .routine(routine)
                .recordDate(TODAY)
                .status(RoutineRecordStatus.PENDING)
                .build();
        stubCompleteToActuallyComplete();

        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
        when(routineRecordProvisioner.find(ROUTINE_ID, TODAY))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(raceWinnerRecord));
        when(routineRecordProvisioner.create(routine, TODAY))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        routineRecordService.completeRoutineByAlternativeMission(ROUTINE_ID, TODAY);

        assertThat(raceWinnerRecord.getStatus()).isEqualTo(RoutineRecordStatus.COMPLETED);
        verify(streakService).updateStreak(USER_ID, ROUTINE_ID, TODAY);
    }

    @Test
    void 동시_완료_요청으로_낙관적_락_충돌이_나면_조용히_종료한다() {
        Routine routine = routineWithOwner();
        RoutineRecord pending = RoutineRecord.builder()
                .routine(routine)
                .recordDate(TODAY)
                .status(RoutineRecordStatus.PENDING)
                .build();

        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
        when(routineRecordProvisioner.find(ROUTINE_ID, TODAY)).thenReturn(Optional.of(pending));
        doThrow(new ObjectOptimisticLockingFailureException(RoutineRecord.class, 1L))
                .when(routineRecordProvisioner).complete(pending);

        routineRecordService.completeRoutineByAlternativeMission(ROUTINE_ID, TODAY);

        verify(streakService, never()).updateStreak(anyLong(), anyLong(), any());
    }

    @Test
    void completeRoutine에서도_낙관적_락_충돌시_이미완료된루틴_예외로_변환한다() {
        Routine routine = routineWithOwner();
        RoutineRecord pending = RoutineRecord.builder()
                .routine(routine)
                .recordDate(TODAY)
                .status(RoutineRecordStatus.PENDING)
                .build();

        when(routineRepository.findById(ROUTINE_ID)).thenReturn(Optional.of(routine));
        when(routineRecordProvisioner.find(eq(ROUTINE_ID), any())).thenReturn(Optional.of(pending));
        doThrow(new ObjectOptimisticLockingFailureException(RoutineRecord.class, 1L))
                .when(routineRecordProvisioner).complete(pending);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> routineRecordService.completeRoutine(ROUTINE_ID))
                .isInstanceOf(com.likelion.team4.global.exception.CustomException.class)
                .extracting(e -> ((com.likelion.team4.global.exception.CustomException) e).getErrorCode())
                .isEqualTo(com.likelion.team4.global.exception.ErrorCode.ALREADY_COMPLETED_ROUTINE);
    }
}
