package com.polyHub.board.free.dto;

import lombok.Data;

@Data
public class CommentWriteDto {
    private Long postId;      // 원본 게시글 ID
    private Long parentId;    // 부모 댓글 ID (답글일 경우)
    private String content;   // 댓글 내용
}