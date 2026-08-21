package com.likelion.team4.domain.routine.entity;

import com.likelion.team4.domain.routine.entity.enums.RoutineRecordStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_routine_record_routine_id_record_date",
                columnNames = {"routine_id", "record_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoutineRecordStatus status;

    @Version
    private Long version;

    public void complete() {
        this.status = RoutineRecordStatus.COMPLETED;
    }

    public void markIncomplete() {
        this.status = RoutineRecordStatus.INCOMPLETE;
    }
}