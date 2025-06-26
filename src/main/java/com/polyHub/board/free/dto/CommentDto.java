package com.polyHub.board.free.dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private Long parentId; // [추가] 부모 댓글의 ID
    private List<CommentDto> replies; // 답글 목록
}