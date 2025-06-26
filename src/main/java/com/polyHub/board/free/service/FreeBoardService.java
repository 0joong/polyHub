package com.polyHub.board.free.service;

import com.polyHub.board.free.dto.*;
import com.polyHub.board.free.entity.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FreeBoardService {

    /**
     * [수정] 검색 키워드를 받을 수 있도록 파라미터 추가
     */
    Page<FreeBoardListDto> findPosts(Pageable pageable, String keyword);
    Page<FreeBoardListDto> findPosts(Pageable pageable);
    FreeBoardDetailDto findPostById(Long postId, Long memberId);
    Long writePost(FreeBoardWriteDto writeDto, Long memberId);

    /**
     * [추가] 댓글 또는 답글을 작성합니다.
     * @param writeDto 댓글 작성 정보
     * @param memberId 작성자 ID
     * @return 생성된 CommentDto
     */
    CommentDto writeComment(CommentWriteDto writeDto, Long memberId);

    /**
     * [추가] 게시글을 수정합니다.
     * @param postId 수정할 게시글 ID
     * @param updateDto 수정할 내용
     * @param memberId 현재 로그인한 사용자 ID (작성자 확인용)
     */
    void updatePost(Long postId, FreeBoardWriteDto updateDto, Long memberId);

    /**
     * [추가] 게시글을 삭제합니다.
     * @param postId 삭제할 게시글 ID
     * @param memberId 현재 로그인한 사용자 ID (작성자 확인용)
     */
    void deletePost(Long postId, Long memberId);

    /**
     * [추가] 게시글에 대한 추천/비추천을 처리합니다.
     * @param postId 게시글 ID
     * @param memberId 사용자 ID
     * @param reactionType 반응 유형 (LIKE 또는 DISLIKE)
     * @return 업데이트된 추천/비추천 정보
     */
    ReactionDto reactToPost(Long postId, Long memberId, ReactionType reactionType);
}
