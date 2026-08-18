package com.likelion.team4.domain.report.service;

import com.likelion.team4.domain.chat.repository.AlternativeMissionRepository;
import com.likelion.team4.domain.report.dto.response.*;
import com.likelion.team4.domain.routine.entity.Routine;
import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.routine.repository.RoutineRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final RoutineRepository routineRepository;
    private final RoutineRecordRepository routineRecordRepository;
    private final AlternativeMissionRepository alternativeMissionRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;

    // 일간 리포트 조회
    public DailyReportResponse getDailyReport(
            Long userId,
            LocalDate date
    ) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        // 2. 해당 날짜에 수행 대상인 루틴만 DB에서 조회
        String day = getDay(date);

        List<Routine> targetRoutines =
                routineRepository.findTargetRoutines(
                        userId,
                        date,
                        day
                );

        // 3. 루틴별 완료 여부
        // 3. 대상 루틴 ID 추출
        List<Long> routineIds =
                targetRoutines.stream()
                        .map(Routine::getId)
                        .toList();

        // 4. 해당 루틴들의 오늘 기록을 한 번에 조회
        List<RoutineRecord> records =
                routineIds.isEmpty()
                        ? List.of()
                        : routineRecordRepository.findAllByRoutine_IdInAndRecordDate(
                        routineIds,
                        date
                );

        // 5. 루틴 ID별 완료 여부를 Map으로 변환
        Map<Long, Boolean> completedByRoutineId =
                records.stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        record -> record.getRoutine().getId(),
                                        RoutineRecord::isCompleted
                                )
                        );

        // 6. 루틴별 완료 여부를 메모리에서 확인
        List<DailyRoutineResponse> routineResponses =
                targetRoutines.stream()
                        .map(routine ->
                                DailyRoutineResponse.builder()
                                        .routineId(routine.getId())
                                        .title(routine.getTitle())
                                        .completed(
                                                completedByRoutineId.getOrDefault(
                                                        routine.getId(),
                                                        false
                                                )
                                        )
                                        .build()
                        )
                        .toList();

        // 7. 전체 루틴 수
        int totalRoutineCount = routineResponses.size();

        // 8. 완료 루틴 수
        int completedRoutineCount =
                (int) routineResponses.stream()
                        .filter(DailyRoutineResponse::isCompleted)
                        .count();

        // 9. 완료율
        int completionRate =
                totalRoutineCount == 0
                        ? 0
                        : (completedRoutineCount * 100)
                          / totalRoutineCount;

        // 10. 완료된 대체 미션 수
        int alternativeMissionCount =
                (int) alternativeMissionRepository
                        .countByAiChat_RoutineLog_Routine_User_IdAndMissionDateAndStatus(
                                userId,
                                date,
                                "COMPLETED"
                        );

        // 11. 현재 연속 기록
        int currentStreak = user.getCurrentStreak();

        return DailyReportResponse.builder()
                .reportId(date.toEpochDay())
                .date(date)
                .totalRoutineCount(totalRoutineCount)
                .completedRoutineCount(completedRoutineCount)
                .alternativeMissionCount(alternativeMissionCount)
                .completionRate(completionRate)
                .currentStreak(currentStreak)
                .routines(routineResponses)
                .build();
    }

    // 요일 변환
    private String getDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

    // 주간 리포트 조회
    // 기준 날짜를 포함한 최근 7일 조회
    // 주간 리포트 조회
// 기준 날짜를 포함한 최근 7일 조회
    public WeeklyReportResponse getWeeklyReport(
            Long userId,
            LocalDate date
    ) {

        // date를 해당 주의 마지막 날짜로 사용
        LocalDate endDate = date;
        LocalDate startDate = date.minusDays(6);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        // 2. 해당 사용자의 루틴 조회 - 1회
        List<Routine> routines =
                routineRepository.findWeeklyTargetRoutines(
                        userId,
                        startDate,
                        endDate
                );

        // 3. 해당 기간의 루틴 기록 전체 조회 - 1회
        List<RoutineRecord> records =
                routineRecordRepository.findAllByRoutine_User_IdAndRecordDateBetween(
                        userId,
                        startDate,
                        endDate
                );

        // 날짜별 완료된 루틴 ID 저장
        Map<LocalDate, Set<Long>> completedRoutineIdsByDate =
                new HashMap<>();

        for (RoutineRecord record : records) {
            if (!record.isCompleted()) {
                continue;
            }

            completedRoutineIdsByDate
                    .computeIfAbsent(
                            record.getRecordDate(),
                            key -> new HashSet<>()
                    )
                    .add(record.getRoutine().getId());
        }

        // 전체 루틴 수
        int totalRoutineCount = 0;

        // 전체 완료 루틴 수
        int completedRoutineCount = 0;

        // 일별 리포트
        List<WeeklyDailyReportResponse> weeklyDailyReports =
                new java.util.ArrayList<>();

        // 4. 최근 7일을 Java에서 계산
        for (int i = 0; i < 7; i++) {

            LocalDate currentDate = startDate.plusDays(i);
            String day = getDay(currentDate);

            // 해당 날짜에 실제 수행 대상인 루틴
            List<Routine> targetRoutines =
                    routines.stream()
                            .filter(routine ->
                                    routine.getStartDate() == null
                                            || !currentDate.isBefore(
                                            routine.getStartDate()
                                    )
                            )
                            .filter(routine ->
                                    routine.getEndDate() == null
                                            || !currentDate.isAfter(
                                            routine.getEndDate()
                                    )
                            )
                            .filter(Routine::isActive)
                            .filter(routine ->
                                    routine.getRepeatDays() != null
                                            && routine.getRepeatDays().contains(day)
                            )
                            .toList();

            // 해당 날짜의 전체 루틴 수
            int dailyTotalRoutineCount =
                    targetRoutines.size();

            // 해당 날짜의 완료된 루틴 ID
            Set<Long> completedRoutineIds =
                    completedRoutineIdsByDate.getOrDefault(
                            currentDate,
                            Set.of()
                    );

            // 해당 날짜의 완료 루틴 수
            int dailyCompletedRoutineCount =
                    (int) targetRoutines.stream()
                            .filter(routine ->
                                    completedRoutineIds.contains(
                                            routine.getId()
                                    )
                            )
                            .count();

            // 해당 날짜의 완료율
            int dailyCompletionRate =
                    dailyTotalRoutineCount == 0
                            ? 0
                            : (dailyCompletedRoutineCount * 100)
                              / dailyTotalRoutineCount;

            // 주간 전체에 누적
            totalRoutineCount += dailyTotalRoutineCount;
            completedRoutineCount += dailyCompletedRoutineCount;

            // 일별 응답 추가
            weeklyDailyReports.add(
                    WeeklyDailyReportResponse.builder()
                            .date(currentDate)
                            .completionRate(dailyCompletionRate)
                            .build()
            );
        }

        // 5. 전체 완료율
        int completionRate =
                totalRoutineCount == 0
                        ? 0
                        : (completedRoutineCount * 100)
                          / totalRoutineCount;

        // 6. 가장 완료율이 높은 날
        LocalDate bestDay =
                weeklyDailyReports.stream()
                        .filter(report ->
                                report.getCompletionRate() > 0
                        )
                        .max(
                                Comparator
                                        .comparingInt(
                                                WeeklyDailyReportResponse
                                                        ::getCompletionRate
                                        )
                                        .thenComparing(
                                                WeeklyDailyReportResponse::getDate
                                        )
                        )
                        .map(WeeklyDailyReportResponse::getDate)
                        .orElse(null);

        return WeeklyReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRoutineCount(totalRoutineCount)
                .completedRoutineCount(completedRoutineCount)
                .completionRate(completionRate)
                .bestDay(bestDay)
                .currentStreak(user.getCurrentStreak())
                .dailyReports(weeklyDailyReports)
                .build();
    }

    public StreakResponse getStreak(Long userId) {
        return streakService.getStreak(userId);
    }

    public List<ReportHistoryResponse> getReportHistory(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        LocalDate today = LocalDate.now();

        List<Routine> routines =
                routineRepository.findAllByUser_IdAndDeletedAtIsNull(userId);

        // 루틴이 실제 수행 대상이었던 과거 날짜들을 수집
        LocalDate earliestDate = routines.stream()
                .map(Routine::getStartDate)
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(today);

        if (earliestDate.isAfter(today.minusDays(1))) {
            return List.of();
        }

        List<LocalDate> dates =
                earliestDate.datesUntil(today)
                        .sorted(java.util.Comparator.reverseOrder())
                        .toList();

        return dates.stream()
                .map(date -> getDailyReport(userId, date))
                .filter(report -> report.getTotalRoutineCount() > 0)
                .map(report -> ReportHistoryResponse.builder()
                        .reportId(report.getDate().toEpochDay())
                        .date(report.getDate())
                        .completionRate(report.getCompletionRate())
                        .completedRoutineCount(report.getCompletedRoutineCount())
                        .totalRoutineCount(report.getTotalRoutineCount())
                        .alternativeMissionCount(report.getAlternativeMissionCount())
                        .build())
                .toList();
    }

    public DailyReportResponse getReportDetail(
            Long userId,
            Long reportId
    ) {

        // reportId를 날짜로 변환
        LocalDate date = LocalDate.ofEpochDay(reportId);

        // 오늘 이후의 리포트는 조회 불가
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "존재하지 않는 리포트입니다."
            );
        }

        // 기존 일간 리포트 조회 로직 재사용
        return getDailyReport(userId, date);
    }
}