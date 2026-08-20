package com.likelion.team4.domain.chat.service;

import com.likelion.team4.domain.chat.entity.AiChat;
import com.likelion.team4.domain.chat.repository.AiChatRepository;
import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * RoutineLog당 AiChat 조회/생성을 각각 독립된 트랜잭션(REQUIRES_NEW)으로 격리한다.
 * RoutineLogProvisioner와 동일한 이유 - 유니크 제약 위반으로 실패해도
 * 호출한 쪽의 트랜잭션/영속성 컨텍스트가 오염되지 않도록 하기 위함.
 */
@Component
@RequiredArgsConstructor
public class AiChatProvisioner {

    private final AiChatRepository aiChatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<AiChat> findExisting(Long routineLogId) {
        return aiChatRepository.findTopByRoutineLogIdOrderByCreatedAtDesc(routineLogId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiChat create(RoutineLog routineLog) {
        return aiChatRepository.save(
                AiChat.builder()
                        .routineLog(routineLog)
                        .build()
        );
    }
}
