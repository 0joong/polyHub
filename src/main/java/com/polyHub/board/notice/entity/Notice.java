package com.polyHub.board.notice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 100)
    private String writer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isPinned; // 상단 고정 여부

    private Long view_cnt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // 최초 저장할 때 작성일을 현재 시각으로 세팅
        this.view_cnt = 0L;                    // 조회수를 0으로 초기화
        if (this.isPinned == null) this.isPinned = false; // 상단 고정 여부 null이면 false로 초기화
    }


    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정할 때마다 수정일을 현재 시각으로 업데이트
    }

}
