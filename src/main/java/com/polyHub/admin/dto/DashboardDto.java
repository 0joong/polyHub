package com.polyHub.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDto {
    private long newUsersCount24h;     // 24시간 내 신규 가입자 수
    private long totalMembersCount;    // 총 회원 수
    private long monthlyActiveUsersCount; // 월간 활성 사용자 수 (MAU)
    private long dailyPostsCount;      // 일일 새 게시글 수
    private long dailyCommentsCount;   // 일일 새 댓글 수
}