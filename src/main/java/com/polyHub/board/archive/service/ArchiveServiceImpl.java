package com.polyHub.board.archive.service;

import com.polyHub.board.archive.dto.ArchiveDto;
import com.polyHub.board.archive.dto.ArchiveListDto;
import com.polyHub.board.archive.dto.FileDto;
import com.polyHub.board.archive.entity.Archive;
import com.polyHub.board.archive.entity.ArchiveFile;
import com.polyHub.board.archive.repository.ArchiveFileRepository;
import com.polyHub.board.archive.repository.ArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final ArchiveFileRepository archiveFileRepository;

    // application.properties 또는 application.yml에 파일 저장 경로를 설정해야 합니다.
    // 예: file.upload-dir=C:/uploads/archive
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public Page<ArchiveListDto> findArchives(Pageable pageable, String keyword) {
        Page<Archive> archives;

        if (StringUtils.hasText(keyword)) {
            // 검색어가 있으면 제목 또는 내용에서 검색
            archives = archiveRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        } else {
            // 검색어가 없으면 전체 목록 조회
            archives = archiveRepository.findAll(pageable);
        }

        // Page<Archive>를 Page<ArchiveListDto>로 변환하여 반환
        return archives.map(this::convertToDto);
    }

    /**
     * [수정] Archive 엔티티를 ArchiveListDto로 변환하는 헬퍼 메소드
     * 엔티티와 DTO의 필드명에 맞게 수정되었습니다.
     */
    private ArchiveListDto convertToDto(Archive archive) {
        return ArchiveListDto.builder()
                .id(archive.getId())
                .title(archive.getTitle())
                .writer(archive.getWriter()) // 엔티티의 writer 필드 사용
                .createdAt(archive.getCreatedAt())
                .viewCount(archive.getViewCount())
                .hasFiles(!archive.getArchiveFiles().isEmpty()) // DTO의 hasFiles 필드에 맞게 archiveFiles 사용
                .build();
    }

    // 상세 조회
    @Override
    @Transactional
    public ArchiveDto findArchiveById(Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + archiveId));

        // 조회수 증가
        archive.setViewCount(archive.getViewCount() + 1);

        return convertToDtoWithFiles(archive); // 상세 보기에서는 파일 목록 포함
    }

    // 글 저장 (파일 저장 포함)
    @Override
    @Transactional
    public Long save(ArchiveDto archiveDto, List<MultipartFile> files) throws IOException {
        // 1. 게시글 텍스트 정보 저장
        Archive archive = Archive.builder()
                .title(archiveDto.getTitle())
                .content(archiveDto.getContent())
                .writer(archiveDto.getWriter())
                .build();
        Archive savedArchive = archiveRepository.save(archive);

        // 2. 파일 정보 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    storeFile(file, savedArchive);
                }
            }
        }
        return savedArchive.getId();
    }

    // 글 수정 (파일 수정 포함)
    @Override
    @Transactional
    public void update(Long archiveId, ArchiveDto archiveDto, List<Long> deleteFileIds, List<MultipartFile> newFiles) throws IOException {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + archiveId));

        // 1. 텍스트 정보 업데이트
        archive.setTitle(archiveDto.getTitle());
        archive.setContent(archiveDto.getContent());

        // 2. 기존 파일 삭제
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            List<ArchiveFile> filesToDelete = archiveFileRepository.findAllById(deleteFileIds);
            for (ArchiveFile file : filesToDelete) {
                // 물리적 파일 삭제
                Files.deleteIfExists(Paths.get(uploadDir + File.separator + file.getStoredFileName()));
            }
            // DB에서 파일 정보 삭제
            archiveFileRepository.deleteAllInBatch(filesToDelete);
        }

        // 3. 새로운 파일 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    storeFile(file, archive);
                }
            }
        }
    }

    // 글 삭제 (물리적 파일 포함)
    @Override
    @Transactional
    public void delete(Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + archiveId));

        // 1. 서버에 저장된 물리적 파일들 삭제
        archive.getArchiveFiles().forEach(file -> {
            try {
                Files.deleteIfExists(Paths.get(uploadDir + File.separator + file.getStoredFileName()));
            } catch (IOException e) {
                // 예외 처리 (로깅 등)
                e.printStackTrace();
            }
        });

        // 2. 게시글 삭제 (Cascade 설정으로 파일 DB 정보도 함께 삭제됨)
        archiveRepository.delete(archive);
    }

    // --- Private Helper Methods ---

    // 파일을 서버에 저장하고 DB에 파일 정보를 기록하는 메소드
    private void storeFile(MultipartFile multipartFile, Archive archive) throws IOException {
        String originalFileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;

        // 파일을 서버의 지정된 경로에 저장
        Path targetLocation = Paths.get(uploadDir + File.separator + storedFileName);
        Files.copy(multipartFile.getInputStream(), targetLocation);

        ArchiveFile archiveFile = ArchiveFile.builder()
                .archive(archive)
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileSize(multipartFile.getSize())
                .build();

        archiveFileRepository.save(archiveFile);
    }

    // Entity -> DTO 변환 (파일 목록 포함)
    private ArchiveDto convertToDtoWithFiles(Archive archive) {
        List<FileDto> fileDtos = archive.getArchiveFiles().stream()
                .map(file -> new FileDto(file.getId(), file.getOriginalFileName()))
                .collect(Collectors.toList());

        return ArchiveDto.builder()
                .id(archive.getId())
                .title(archive.getTitle())
                .content(archive.getContent())
                .writer(archive.getWriter())
                .viewCount(archive.getViewCount())
                .createdAt(archive.getCreatedAt())
                .files(fileDtos) // 파일 목록 추가
                .build();
    }

    // Entity -> DTO 변환 (파일 목록 미포함, 목록 페이지용)
    private ArchiveDto convertToDtoSimple(Archive archive) {
        return ArchiveDto.builder()
                .id(archive.getId())
                .title(archive.getTitle())
                .writer(archive.getWriter())
                .viewCount(archive.getViewCount())
                .createdAt(archive.getCreatedAt())
                .files(archive.getArchiveFiles().isEmpty() ? Collections.emptyList() : List.of(new FileDto())) // 파일 존재 여부만 확인
                .build();
    }

    /**
     * 파일 ID로 첨부파일 정보를 조회합니다.
     * @param fileId 파일의 고유 ID
     * @return 조회된 파일 엔티티
     */
    @Override
    public ArchiveFile findFileById(Long fileId) {
        return archiveFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. id=" + fileId));
    }
}
