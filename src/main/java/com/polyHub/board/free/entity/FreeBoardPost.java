package com.polyHub.board.free.entity;

import com.polyHub.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FREE_BOARD")
@Getter
@Setter // 서비스에서 count를 변경하기 위해 Setter를 사용합니다.
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeBoardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FREE_BOARD_SEQ_GEN")
    @SequenceGenerator(name = "FREE_BOARD_SEQ_GEN", sequenceName = "FREE_BOARD_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Builder.Default
    private int viewCount = 0;
    @Builder.Default
    private int likeCount = 0;
    @Builder.Default
    private int dislikeCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * [추가] 게시글에 달린 댓글 목록과의 관계 설정
     * 게시글이 삭제되면 댓글도 함께 삭제되도록 cascade 옵션을 설정합니다.
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FreeBoardComment> comments = new ArrayList<>();
}