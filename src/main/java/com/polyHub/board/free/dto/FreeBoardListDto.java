package com.polyHub.board.free.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FreeBoardListDto {
    private Long id;
    private String title;
    private String writer;
    private LocalDateTime createdAt;
    private int viewCount;
    private int likeCount;
    private int commentCount; // 댓글 수 추가
}
