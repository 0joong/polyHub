package com.polyHub.board.archive.repository;

import com.polyHub.board.archive.entity.Archive;
import com.polyHub.board.archive.entity.ArchiveFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchiveFileRepository extends JpaRepository<ArchiveFile, Long> {

    /**
     * 게시글 ID(archiveId)를 기준으로 모든 첨부파일을 조회합니다.
     * @param archiveId 원본 게시글의 ID
     * @return 해당 게시글에 첨부된 파일 목록
     */
    List<ArchiveFile> findByArchiveId(Long archiveId);

    /**
     * 주어진 ID 목록에 해당하는 모든 첨부파일을 삭제합니다.
     * @param ids 삭제할 파일들의 ID 목록
     */
    void deleteByArchiveId(Long archiveId);

}