package com.likelion.team4.domain.routine.service;

import com.likelion.team4.domain.routine.dto.request.RoutineRequest;
import com.likelion.team4.domain.routine.dto.response.RoutineResponse;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    public List<RoutineResponse> getRoutinesByDay(Long userId, String day) {
        // 1. 레포지토리에서 유저 ID를 기준으로 루틴 엔티티 리스트 조회
        List<Routine> routines = routineRepository.findAllByUser_IdAndRepeatDaysContainingAndDeletedAtIsNull(userId, day);
        // 2. 조회된 엔티티 리스트를 DTO 리스트로 변환하여 반환
        return routines.stream()
                .map(RoutineResponse::from)
                .toList();
    }

    public List<RoutineResponse> getAllRoutines(Long userId) {
        List<Routine> routines = routineRepository.findAllByUser_IdAndDeletedAtIsNull(userId);
        return routines.stream()
                .map(RoutineResponse::from)
                .toList();
    }

    @Transactional // 쓰기 작업이므로 필수
    public RoutineResponse createRoutine(Long userId, RoutineRequest request) {
        // 1. 유저 조회 (유저가 없을 경우 예외 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. Request DTO를 바탕으로 Routine 엔티티 생성
        Routine routine = Routine.builder()
                .user(user)
                .title(request.getTitle())
                .performTime(request.getPerformTime())
                .repeatDays(request.getRepeatDays())
                .alarm(request.getAlarm())
                .active(request.getActive())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .alarmTime(request.getAlarmTime())
                .repeatType(request.getRepeatType())
                .repeatCount(request.getRepeatCount())
                .build();

        // 3. 데이터베이스에 저장
        Routine savedRoutine = routineRepository.save(routine);

        // 4. 저장된 엔티티를 DTO로 변환하여 반환
        return RoutineResponse.from(savedRoutine);
    }

    public RoutineResponse getRoutine(Long userId, Long routineId) {
        // 1. 404와 403을 구분하기 위해 루틴 ID로만 먼저 조회
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 삭제된 루틴인지 검증 (소프트 딜리트 확인)
        if (routine.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 3. 본인의 루틴인지 권한 검증
        if (!routine.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 4. 조회된 엔티티를 DTO로 변환하여 반환
        return RoutineResponse.from(routine);
    }

    @Transactional // 쓰기 작업이므로 트랜잭션 적용
    public RoutineResponse updateRoutine(Long userId, Long routineId, RoutineRequest request) {
        // 1. 루틴 ID로 조회
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 삭제된 루틴인지 검증 (소프트 딜리트 확인)
        if (routine.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 3. 본인의 루틴인지 권한 검증
        if (!routine.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 4. 엔티티 수정 (더티 체킹으로 인해 별도의 save 호출 생략 가능)
        routine.update(
                request.getTitle(),
                request.getPerformTime(),
                request.getRepeatDays(),
                request.getAlarm(),
                request.getActive(),
                request.getStartDate(),
                request.getEndDate(),
                request.getAlarmTime(),
                request.getRepeatType(),
                request.getRepeatCount()
        );

        // 5. 수정된 엔티티를 DTO로 변환하여 반환
        return RoutineResponse.from(routine);
    }

    @Transactional // 쓰기 작업이므로 트랜잭션 적용
    public RoutineResponse updateActiveStatus(Long userId, Long routineId, Boolean active) {
        // 1. 루틴 ID로 조회
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 2. 삭제된 루틴인지 검증 (소프트 딜리트 확인)
        if (routine.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // 3. 본인의 루틴인지 권한 검증
        if (!routine.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 4. 엔티티 상태 변경
        routine.updateActive(active);

        // 5. 수정된 엔티티를 DTO로 변환하여 반환
        return RoutineResponse.from(routine);
    }
}
