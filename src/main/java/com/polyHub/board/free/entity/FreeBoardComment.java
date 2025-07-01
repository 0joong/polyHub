package com.polyHub.board.free.entity;

import com.polyHub.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FREE_BOARD_COMMENT")
@Getter
@Setter // 답글 관계 설정 시 필요할 수 있음
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeBoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FREE_BOARD_COMMENT_SEQ_GEN")
    @SequenceGenerator(name = "FREE_BOARD_COMMENT_SEQ_GEN", sequenceName = "FREE_BOARD_COMMENT_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private FreeBoardPost post; // 원본 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member; // 댓글 작성자

    // 부모 댓글 ID. 이 값이 null이면 일반 댓글, 값이 있으면 답글(대댓글).
    // private Long parentId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Builder.Default
    private int likeCount = 0;

    @Builder.Default
    private int dislikeCount = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // [핵심 4] 부모 댓글이 삭제될 때, 자식 댓글(답글)도 함께 삭제합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private FreeBoardComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeBoardComment> children = new ArrayList<>();
}