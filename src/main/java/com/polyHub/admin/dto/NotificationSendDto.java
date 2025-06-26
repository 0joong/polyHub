package com.polyHub.admin.dto;

import lombok.Data;

@Data
public class NotificationSendDto {
    private String targetRole; // 발송 대상 역할 (ALL, USER, MANAGER, ADMIN)
    private String message;    // 알림 내용
    private String link;       // 연결 링크 (선택)
}