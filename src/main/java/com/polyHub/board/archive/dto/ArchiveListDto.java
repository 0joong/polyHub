package com.polyHub.board.archive.dto; // 패키지 경로는 실제 프로젝트에 맞게 수정해주세요.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 자료실 게시판 목록 조회 시 사용되는 DTO입니다.
 * 목록 표시에 필요한 최소한의 정보를 담습니다.
 */
@Getter
@Builder
@AllArgsConstructor // [추가] 모든 필드를 사용하는 생성자를 자동으로 만듭니다.
public class ArchiveListDto {

    private final Long id;
    private final String title;
    private final String writer;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final boolean hasFiles; // 파일 존재 여부

    // [제거] @Builder와 @AllArgsConstructor 애너테이션이 생성자 코드를 대체합니다.
}
