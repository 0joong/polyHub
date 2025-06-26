package com.polyHub.board.archive.dto;

import com.polyHub.board.archive.entity.Archive;
import lombok.*;
import java.time.LocalDateTime;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글의 전체 정보를 담는 DTO 입니다.
 * Controller와 View 사이에서 데이터를 전달하는 데 사용됩니다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveDto {

    private Long id;
    private String title;
    private String content;
    private String writer;
    private int viewCount;
    private LocalDateTime createdAt;

    // 이 게시글에 첨부된 파일 목록을 담습니다.
    private List<FileDto> files;
}
