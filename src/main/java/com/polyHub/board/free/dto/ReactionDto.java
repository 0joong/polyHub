package com.polyHub.board.free.dto;

import lombok.Data;

@Data
public class ReactionDto {
    private int likeCount;
    private int dislikeCount;
    private String userReaction; // 현재 유저의 반응 상태 (LIKE, DISLIKE, NONE)
}