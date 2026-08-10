package com.likelion.team4.domain.chat.entity;

import com.likelion.team4.domain.routinelog.entity.RoutineLog;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_log_id", nullable = false)
    private RoutineLog routineLog;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public AiChat(RoutineLog routineLog) {
        this.routineLog = routineLog;
        this.createdAt = LocalDateTime.now();
    }
}
