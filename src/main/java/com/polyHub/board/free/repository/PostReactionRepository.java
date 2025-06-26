package com.polyHub.board.free.repository;

import com.polyHub.board.free.entity.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    Optional<PostReaction> findByMemberIdAndPostId(Long memberId, Long postId);
}