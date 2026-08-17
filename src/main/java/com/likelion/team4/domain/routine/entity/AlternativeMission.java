package com.likelion.team4.domain.routine.entity;

import com.likelion.team4.domain.routine.entity.enums.MissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "RoutineAlternativeMission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AlternativeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Column(nullable = false)
    private boolean completed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status;

    public void complete() {
        this.status = MissionStatus.COMPLETED;
    }

}