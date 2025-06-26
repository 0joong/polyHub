package com.polyHub.board.archive.service;

import com.polyHub.board.archive.dto.ArchiveDto;
import com.polyHub.board.archive.dto.ArchiveListDto;
import com.polyHub.board.archive.entity.ArchiveFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface ArchiveService {

    /**
     * 자료실 목록을 검색 조건과 함께 조회합니다.
     * @param pageable 페이징 정보
     * @param keyword 검색 키워드
     * @return 페이징된 자료실 목록
     */
    Page<ArchiveListDto> findArchives(Pageable pageable, String keyword);

    // 상세 조회 (조회수 증가 포함)
    ArchiveDto findArchiveById(Long archiveId);

    // 글 저장
    Long save(ArchiveDto archiveDto, List<MultipartFile> files) throws Exception;

    // 글 수정
    void update(Long archiveId, ArchiveDto archiveDto, List<Long> deleteFileIds, List<MultipartFile> newFiles) throws Exception;

    // 글 삭제
    void delete(Long archiveId);

    // 파일 다운로드 로직은 별도의 FileService에서 처리하는 것이 좋습니다.
    ArchiveFile findFileById(Long fileId);
}


