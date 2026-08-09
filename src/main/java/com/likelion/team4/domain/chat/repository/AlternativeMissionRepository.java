package com.likelion.team4.domain.chat.repository;

import com.likelion.team4.domain.chat.entity.AlternativeMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlternativeMissionRepository extends JpaRepository<AlternativeMission, Long> {

    // 특정 대화의 미션 목록 조회
    List<AlternativeMission> findByAiChatId(Long chatId);

    // 특정 대화의 PENDING 상태 미션 조회
    Optional<AlternativeMission> findByAiChatIdAndStatus(Long chatId, String status);
}