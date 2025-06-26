package com.polyHub.board.free.entity;

import com.polyHub.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FREE_POST_REACTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MEMBER_ID", "POST_ID"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FREE_POST_REACTION_SEQ_GEN")
    @SequenceGenerator(name = "FREE_POST_REACTION_SEQ_GEN", sequenceName = "FREE_POST_REACTION_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_ID", nullable = false)
    private FreeBoardPost post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reactionType;
}
