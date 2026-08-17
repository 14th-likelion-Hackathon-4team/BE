package com.likelion.team4.domain.report.service;

import com.likelion.team4.domain.routine.entity.RoutineRecord;
import com.likelion.team4.domain.routine.repository.RoutineRecordRepository;
import com.likelion.team4.domain.user.entity.User;
import com.likelion.team4.domain.user.repository.UserRepository;
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
    public void updateStreak(Long userId, LocalDate completedDate) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        Set<LocalDate> completedDates =
                getCompletedDates(userId, completedDate);

        int currentStreak =
                calculateCurrentStreak(completedDates, completedDate);

        int maxStreak =
                calculateMaxStreak(completedDates);

        user.updateStreak(currentStreak, maxStreak);
    }

    /**
     * 연속 기록 조회
     */
    @Transactional(readOnly = true)
    public com.likelion.team4.domain.report.dto.response.StreakResponse getStreak(
            Long userId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 사용자입니다.")
                );

        LocalDate today = LocalDate.now();

        Set<LocalDate> completedDates =
                getCompletedDates(userId, today);

        LocalDate lastCompletedDate =
                completedDates.stream()
                        .max(LocalDate::compareTo)
                        .orElse(null);

        int currentStreak =
                lastCompletedDate == null
                        ? 0
                        : calculateCurrentStreak(
                        completedDates,
                        lastCompletedDate
                );

        int maxStreak =
                calculateMaxStreak(completedDates);

        LocalDate startedAt = null;

        if (lastCompletedDate != null) {
            startedAt = lastCompletedDate.minusDays(currentStreak - 1);
        }

        boolean isTodayCompleted =
                completedDates.contains(today);

        return com.likelion.team4.domain.report.dto.response.StreakResponse
                .builder()
                .currentStreak(currentStreak)
                .maxStreak(maxStreak)
                .lastCompletedDate(lastCompletedDate)
                .startedAt(startedAt)
                .isTodayCompleted(isTodayCompleted)
                .build();
    }

    /**
     * 사용자의 완료 날짜 조회
     */
    private Set<LocalDate> getCompletedDates(
            Long userId,
            LocalDate date
    ) {

        List<RoutineRecord> records =
                routineRecordRepository
                        .findAllByRoutine_User_IdAndRecordDateLessThanEqualOrderByRecordDateDesc(
                                userId,
                                date
                        );

        return records.stream()
                .filter(RoutineRecord::isCompleted)
                .map(RoutineRecord::getRecordDate)
                .collect(Collectors.toSet());
    }

    /**
     * 특정 날짜부터 연속된 완료 일수
     */
    private int calculateCurrentStreak(
            Set<LocalDate> completedDates,
            LocalDate date
    ) {

        int streak = 0;
        LocalDate currentDate = date;

        while (completedDates.contains(currentDate)) {
            streak++;
            currentDate = currentDate.minusDays(1);
        }

        return streak;
    }

    /**
     * 전체 최대 연속 기록
     */
    private int calculateMaxStreak(
            Set<LocalDate> completedDates
    ) {

        if (completedDates.isEmpty()) {
            return 0;
        }

        int maxStreak = 0;
        int currentStreak = 0;

        LocalDate previousDate = null;

        for (LocalDate date : completedDates.stream()
                .sorted()
                .toList()) {

            if (previousDate != null
                    && date.equals(previousDate.plusDays(1))) {

                currentStreak++;
            } else {
                currentStreak = 1;
            }

            maxStreak = Math.max(maxStreak, currentStreak);
            previousDate = date;
        }

        return maxStreak;
    }
}