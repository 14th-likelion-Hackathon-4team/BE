package com.likelion.team4.domain.chat.repository;

import com.likelion.team4.domain.chat.entity.AiChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiChatRepository extends JpaRepository<AiChat, Long> {

    // 특정 RoutineLog의 대화 목록 조회
    List<AiChat> findByRoutineLogId(Long routineLogId);

    // 특정 RoutineLog의 가장 최근 대화 조회 (재사용 여부 확인용)
    Optional<AiChat> findTopByRoutineLogIdOrderByCreatedAtDesc(Long routineLogId);
}
