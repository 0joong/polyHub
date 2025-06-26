package com.polyHub.board.archive.repository;

import com.polyHub.board.archive.entity.Archive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<Archive, Long> {
    // 제목으로 검색하는 기능 (페이징 포함)
    Page<Archive> findByTitleContaining(String title, Pageable pageable);

    /**
     * 제목 또는 내용에 특정 키워드가 포함된 자료를 페이징하여 검색합니다.
     * @param titleKeyword 제목 검색 키워드
     * @param contentKeyword 내용 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 자료 페이지
     */
    Page<Archive> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);
}