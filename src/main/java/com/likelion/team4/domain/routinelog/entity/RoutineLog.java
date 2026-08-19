package com.likelion.team4.domain.routinelog.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.likelion.team4.domain.routine.entity.Routine;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "routine_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_routine_log_routine_id_log_date",
                columnNames = {"routine_id", "log_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDate logDate;

    private LocalDateTime completedAt;
    private LocalDateTime alarmSentAt;

    @Column(nullable = false)
    private Boolean reminderSent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Builder
    public RoutineLog(Routine routine, LocalDate logDate, String status, boolean reminderSent) {
        this.routine = routine;
        this.logDate = logDate;
        this.status = status;
        this.reminderSent = reminderSent;
        this.createdAt = LocalDateTime.now();
    }
}
