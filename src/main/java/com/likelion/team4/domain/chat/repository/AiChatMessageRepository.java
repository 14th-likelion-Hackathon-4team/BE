package com.likelion.team4.domain.chat.repository;

import com.likelion.team4.domain.chat.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    // 특정 대화의 메시지 목록 조회 (시간순)
    List<AiChatMessage> findByAiChatIdOrderByCreatedAtAsc(Long chatId);

    // 모든 채팅 메시지 내용 삭제
    void deleteAllByAiChat_RoutineLog_Routine_User_Id(Long userId);
}
