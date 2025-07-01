package com.polyHub.board.free.repository;

import com.polyHub.board.free.entity.FreeBoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FreeBoardPostRepository extends JpaRepository<FreeBoardPost, Long> {

    /**
     * [추가] 특정 시각 이후에 작성된 게시글 수를 조회합니다.
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * [추가] 제목 또는 내용에 특정 키워드가 포함된 게시글을 페이징하여 검색합니다.
     * @param titleKeyword 제목에서 찾을 키워드
     * @param contentKeyword 내용에서 찾을 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 페이지
     */
    Page<FreeBoardPost> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    // [추가] 특정 회원이 작성한 모든 게시글을 삭제하기 위한 메소드
    void deleteAllByMemberId(Long memberId);
}