package com.polyHub.crawling.repository;

import com.polyHub.crawling.entity.CrawledNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawledNoticeRepository extends JpaRepository<CrawledNotice, Long> {

    /**
     * 제목이나 내용에 특정 키워드가 포함된 모든 공지사항을 검색합니다.
     * RagService에서 관련 문서를 찾는 데 사용됩니다.
     * @param titleKeyword 제목에서 검색할 키워드
     * @param contentKeyword 내용에서 검색할 키워드
     * @return 검색된 공지사항 목록
     */
    List<CrawledNotice> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword);

    /**
     * 특정 URL의 공지사항이 이미 DB에 존재하는지 확인합니다.
     * 중복 저장을 방지하는 데 사용됩니다.
     * @param noticeUrl 확인할 원본 공지사항 URL
     * @return 존재 여부 (true/false)
     */
    boolean existsByNoticeUrl(String noticeUrl);

}