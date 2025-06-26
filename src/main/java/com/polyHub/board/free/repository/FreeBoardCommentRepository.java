package com.polyHub.board.free.repository;

import com.polyHub.board.free.entity.FreeBoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FreeBoardCommentRepository extends JpaRepository<FreeBoardComment, Long> {
    // 게시글 ID를 기준으로 모든 댓글을 작성일 순으로 조회
    List<FreeBoardComment> findAllByPostIdOrderByCreatedAtAsc(Long postId);

    /**
     * [추가] 특정 시각 이후에 작성된 댓글 수를 조회합니다.
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);
}