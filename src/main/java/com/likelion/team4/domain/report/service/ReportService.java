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
import java.util.stream.IntStream;

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

        // 2. 해당 사용자의 루틴 조회
        List<Routine> routines =
                routineRepository.findAllByUser_IdAndDeletedAtIsNull(userId);

        // 3. 해당 날짜에 실제 수행 대상인 루틴만 필터링
        String day = getDay(date);

        List<Routine> targetRoutines = routines.stream()
                .filter(routine ->
                        routine.getStartDate() == null
                                || !date.isBefore(routine.getStartDate())
                )
                .filter(routine ->
                        routine.getEndDate() == null
                                || !date.isAfter(routine.getEndDate())
                )
                .filter(Routine::isActive)
                .filter(routine ->
                        routine.getRepeatDays() != null
                                && routine.getRepeatDays().contains(day)
                )
                .toList();

        // 4. 루틴별 완료 여부
        List<DailyRoutineResponse> routineResponses =
                targetRoutines.stream()
                        .map(routine -> {

                            boolean completed =
                                    routineRecordRepository
                                            .findByRoutine_IdAndRecordDate(
                                                    routine.getId(),
                                                    date
                                            )
                                            .map(RoutineRecord::isCompleted)
                                            .orElse(false);

                            return DailyRoutineResponse.builder()
                                    .routineId(routine.getId())
                                    .title(routine.getTitle())
                                    .completed(completed)
                                    .build();
                        })
                        .toList();

        // 5. 전체 루틴 수
        int totalRoutineCount = routineResponses.size();

        // 6. 완료 루틴 수
        int completedRoutineCount =
                (int) routineResponses.stream()
                        .filter(DailyRoutineResponse::isCompleted)
                        .count();

        // 7. 완료율
        int completionRate =
                totalRoutineCount == 0
                        ? 0
                        : (completedRoutineCount * 100)
                          / totalRoutineCount;

        // 8. 완료된 대체 미션 수
        int alternativeMissionCount =
                (int) alternativeMissionRepository
                        .countByAiChat_RoutineLog_Routine_User_IdAndMissionDateAndStatus(
                                userId,
                                date,
                                "COMPLETED"
                        );

        // 9. 현재 연속 기록
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
    public WeeklyReportResponse getWeeklyReport(
            Long userId,
            LocalDate date
    ) {

        // date를 해당 주의 마지막 날짜로 사용
        LocalDate endDate = date;
        LocalDate startDate = date.minusDays(6);

        List<DailyReportResponse> dailyReports =
                java.util.stream.IntStream.rangeClosed(0, 6)
                        .mapToObj(i ->
                                getDailyReport(
                                        userId,
                                        startDate.plusDays(i)
                                )
                        )
                        .toList();

        // 전체 루틴 수
        int totalRoutineCount =
                dailyReports.stream()
                        .mapToInt(DailyReportResponse::getTotalRoutineCount)
                        .sum();

        // 완료 루틴 수
        int completedRoutineCount =
                dailyReports.stream()
                        .mapToInt(DailyReportResponse::getCompletedRoutineCount)
                        .sum();

        // 전체 완료율
        int completionRate =
                totalRoutineCount == 0
                        ? 0
                        : (completedRoutineCount * 100) / totalRoutineCount;

        // 가장 완료율이 높은 날
        LocalDate bestDay =
                dailyReports.stream()
                        .filter(report -> report.getTotalRoutineCount() > 0)
                        .max(
                                java.util.Comparator
                                        .comparingInt(DailyReportResponse::getCompletionRate)
                                        .thenComparing(DailyReportResponse::getDate)
                        )
                        .map(DailyReportResponse::getDate)
                        .orElse(null);

        // 현재 연속 기록
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        int currentStreak = user.getCurrentStreak();

        // 주간용 일별 응답으로 변환
        List<WeeklyDailyReportResponse> weeklyDailyReports =
                dailyReports.stream()
                        .map(report ->
                                WeeklyDailyReportResponse.builder()
                                        .date(report.getDate())
                                        .completionRate(report.getCompletionRate())
                                        .build()
                        )
                        .toList();

        return WeeklyReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRoutineCount(totalRoutineCount)
                .completedRoutineCount(completedRoutineCount)
                .completionRate(completionRate)
                .bestDay(bestDay)
                .currentStreak(currentStreak)
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