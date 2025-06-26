package com.polyHub.board.schedule.entity;

import com.polyHub.board.schedule.dto.ScheduleDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SCHEDULE_SEQ_GEN")
    @SequenceGenerator(name = "SCHEDULE_SEQ_GEN", sequenceName = "SCHEDULE_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(length = 100)
    private String content;

    @CreationTimestamp // 엔티티 생성 시 자동으로 현재 시간 저장
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티 수정 시 자동으로 현재 시간 저장
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // DTO를 기반으로 엔티티를 업데이트하는 메소드
    public void update(ScheduleDto dto) {
        this.title = dto.getTitle();
        this.startDate = LocalDate.parse(dto.getStart());
        this.endDate = (dto.getEnd() != null && !dto.getEnd().isEmpty()) ? LocalDate.parse(dto.getEnd()) : null;
        this.content = dto.getContent();
    }
}