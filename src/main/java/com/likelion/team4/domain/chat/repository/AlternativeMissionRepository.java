package com.likelion.team4.domain.chat.repository;

import com.likelion.team4.domain.chat.entity.AlternativeMission;
import com.likelion.team4.domain.chat.entity.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AlternativeMissionRepository extends JpaRepository<AlternativeMission, Long> {

    // 특정 대화의 미션 목록 조회
    List<AlternativeMission> findByAiChatId(Long chatId);

    // 특정 대화의 PENDING 상태 미션 조회
    Optional<AlternativeMission> findByAiChatIdAndStatus(Long chatId, MissionStatus status);

    // 대체 미션 데이터 삭제
    void deleteAllByAiChat_RoutineLog_Routine_User_Id(Long userId);

    long countByAiChat_RoutineLog_Routine_User_IdAndMissionDateAndStatus(
            Long userId,
            LocalDate missionDate,
            MissionStatus status
    );

    Optional<AlternativeMission>
    findTopByAiChat_RoutineLog_Routine_IdAndMissionDateOrderByCreatedAtDesc(
            Long routineId,
            LocalDate missionDate
    );
}