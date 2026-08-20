package com.likelion.team4.domain.chat.repository;

import com.likelion.team4.domain.chat.entity.AiChatMessage;
import com.likelion.team4.domain.chat.entity.enums.MessageRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    // 특정 대화의 메시지 목록 조회 (시간순)
    List<AiChatMessage> findByAiChatIdOrderByCreatedAtAsc(Long chatId);

    // 특정 대화에서 특정 역할의 가장 첫 메시지 조회 (전체 로드 없이 1건만)
    Optional<AiChatMessage> findFirstByAiChatIdAndRoleOrderByCreatedAtAsc(Long chatId, MessageRole role);

    // 모든 채팅 메시지 내용 삭제
    void deleteAllByAiChat_RoutineLog_Routine_User_Id(Long userId);
}
