package com.likelion.team4.domain.report.service;

import com.likelion.team4.domain.report.dto.response.StreakResponse;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import com.likelion.team4.global.exception.CustomException;
import com.likelion.team4.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StreakService {

    private final RoutineRecordRepository routineRecordRepository;
    private final UserRepository userRepository;

    /**
     * 루틴 완료 후 연속 기록 갱신
     */
    public void updateStreak(
            Long userId,
            Long routineId,
            LocalDate completedDate
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        List<RoutineRecord> records =
                routineRecordRepository
                        .findAllByRoutine_IdAndRecordDateLessThanEqualOrderByRecordDateDesc(
                                routineId,
                                completedDate
                        )
                        .stream()
                        .filter(record -> record.getStatus() == RoutineRecordStatus.COMPLETED)
                        .toList();

        RoutineRecord completedRecord =
                records.stream()
                        .filter(record ->
                                record.getRecordDate()
                                        .equals(completedDate)
                        )
                        .findFirst()
                        .orElse(null);

        if (completedRecord == null) {
            return;
        }

        int currentStreak =
                calculateCurrentStreak(
                        records,
                        completedRecord
                );

        // maxStreak은 여기서 다시 계산하지 않는다.
        // User에 저장된 maxStreak과 비교해서
        // 더 클 때만 갱신한다.
        user.updateStreak(currentStreak);
    }

    /**
     * 연속 기록 조회
     */
    @Transactional(readOnly = true)
    public StreakResponse getStreak(
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                                new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();

        List<RoutineRecord> records =
                getCompletedRecords(
                        userId,
                        today
                );

        // 가장 최근 완료 기록
        RoutineRecord latestRecord =
                records.stream()
                        .max(
                                Comparator
                                        .comparing(
                                                RoutineRecord::getRecordDate
                                        )
                                        .thenComparing(
                                                RoutineRecord::getId
                                        )
                        )
                        .orElse(null);

        LocalDate lastCompletedDate =
                latestRecord == null
                        ? null
                        : latestRecord.getRecordDate();

        boolean isTodayCompleted =
                records.stream()
                        .anyMatch(record ->
                                record.getRecordDate()
                                        .equals(today)
                        );

        // 스트릭은 매번 다시 계산하지 않고
        // User에 저장되어 있는 값을 사용
        int currentStreak =
                user.getCurrentStreak();

        int maxStreak =
                user.getMaxStreak();

        return StreakResponse.builder()
                .currentStreak(currentStreak)
                .maxStreak(maxStreak)
                .lastCompletedDate(lastCompletedDate)
                .startedAt(null)
                .isTodayCompleted(isTodayCompleted)
                .build();
    }

    /**
     * 사용자의 완료된 루틴 기록 조회
     */
    private List<RoutineRecord> getCompletedRecords(
            Long userId,
            LocalDate date
    ) {

        return routineRecordRepository
                .findAllByRoutine_User_IdAndRecordDateLessThanEqualOrderByRecordDateDesc(
                        userId,
                        date
                )
                .stream()
                .filter(record -> record.getStatus() == RoutineRecordStatus.COMPLETED)
                .toList();
    }

    /**
     * 현재 스트릭 계산
     *
     * 단순히 하루씩 거슬러 올라가지 않고
     * 해당 루틴의 repeatDays를 기준으로
     * 직전 예정일을 찾아간다.
     */
    private int calculateCurrentStreak(
            List<RoutineRecord> records,
            RoutineRecord latestRecord
    ) {

        Routine routine =
                latestRecord.getRoutine();

        LocalDate currentDate =
                latestRecord.getRecordDate();

        int streak = 1;

        while (true) {

            LocalDate previousScheduledDate =
                    getPreviousScheduledDate(
                            routine,
                            currentDate
                    );

            // 더 이상 예정일이 없으면 종료
            if (previousScheduledDate == null) {
                break;
            }

            boolean completed =
                    records.stream()
                            .anyMatch(record ->
                                    record.getRoutine()
                                            .getId()
                                            .equals(routine.getId())
                                            && record.getRecordDate()
                                            .equals(
                                                    previousScheduledDate
                                            )
                            );

            // 직전 예정일을 수행하지 않았다면
            // 스트릭 종료
            if (!completed) {
                break;
            }

            streak++;

            currentDate =
                    previousScheduledDate;
        }

        return streak;
    }

    /**
     * 현재 스트릭 시작일 계산
     */
    private LocalDate calculateStartedAt(
            List<RoutineRecord> records,
            RoutineRecord latestRecord
    ) {

        Routine routine =
                latestRecord.getRoutine();

        LocalDate currentDate =
                latestRecord.getRecordDate();

        LocalDate startedAt =
                currentDate;

        while (true) {

            LocalDate previousScheduledDate =
                    getPreviousScheduledDate(
                            routine,
                            currentDate
                    );

            if (previousScheduledDate == null) {
                break;
            }

            boolean completed =
                    records.stream()
                            .anyMatch(record ->
                                    record.getRoutine()
                                            .getId()
                                            .equals(routine.getId())
                                            && record.getRecordDate()
                                            .equals(
                                                    previousScheduledDate
                                            )
                            );

            if (!completed) {
                break;
            }

            startedAt =
                    previousScheduledDate;

            currentDate =
                    previousScheduledDate;
        }

        return startedAt;
    }

    /**
     * 오늘 기준 해당 루틴의 가장 최근 예정일
     */
    private LocalDate getLatestScheduledDate(
            Routine routine,
            LocalDate today
    ) {

        // 비활성화된 루틴은 현재 스트릭 대상에서 제외
        if (!routine.isActive()) {
            return null;
        }

        // 아직 시작하지 않은 루틴
        if (routine.getStartDate() != null
                && today.isBefore(
                routine.getStartDate()
        )) {
            return null;
        }

        // 종료된 루틴
        if (routine.getEndDate() != null
                && today.isAfter(
                routine.getEndDate()
        )) {
            return null;
        }

        for (int i = 0; i <= 7; i++) {

            LocalDate candidate =
                    today.minusDays(i);

            if (isScheduledDate(
                    routine,
                    candidate
            )) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * 특정 루틴의 직전 예정일 조회
     */
    private LocalDate getPreviousScheduledDate(
            Routine routine,
            LocalDate currentDate
    ) {

        for (int i = 1; i <= 7; i++) {

            LocalDate candidate =
                    currentDate.minusDays(i);

            if (isScheduledDate(
                    routine,
                    candidate
            )) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * 해당 날짜가 루틴의 실제 수행 예정일인지 확인
     */
    private boolean isScheduledDate(
            Routine routine,
            LocalDate date
    ) {

        // 시작일 이전
        if (routine.getStartDate() != null
                && date.isBefore(
                routine.getStartDate()
        )) {
            return false;
        }

        // 종료일 이후
        if (routine.getEndDate() != null
                && date.isAfter(
                routine.getEndDate()
        )) {
            return false;
        }

        String day =
                getDay(date);

        return routine.getRepeatDays() != null
                && routine.getRepeatDays().contains(day);
    }

    /**
     * 전체 최대 연속 기록
     *
     * 기존 로직은 유지
     */
    private int calculateMaxStreak(
            List<RoutineRecord> records
    ) {

        if (records.isEmpty()) {
            return 0;
        }

        Set<LocalDate> completedDates =
                records.stream()
                        .map(RoutineRecord::getRecordDate)
                        .collect(Collectors.toSet());

        int maxStreak = 0;
        int currentStreak = 0;

        LocalDate previousDate = null;

        for (LocalDate date :
                completedDates.stream()
                        .sorted()
                        .toList()) {

            if (previousDate != null
                    && date.equals(
                    previousDate.plusDays(1)
            )) {

                currentStreak++;

            } else {

                currentStreak = 1;
            }

            maxStreak =
                    Math.max(
                            maxStreak,
                            currentStreak
                    );

            previousDate = date;
        }

        return maxStreak;
    }

    /**
     * 날짜 → 요일 문자열
     */
    private String getDay(
            LocalDate date
    ) {

        return switch (
                date.getDayOfWeek()
                ) {

            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }
}