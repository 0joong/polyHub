package com.polyHub.board.free.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FreeBoardDetailDto {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
    private int viewCount;
    private int likeCount;
    private int dislikeCount;
    private String userReaction; // 현재 사용자의 반응 (LIKE, DISLIKE, NONE)
    private List<CommentDto> comments; // [수정] 댓글 목록 필드 추가
}
