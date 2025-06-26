package com.polyHub.notification.dto; // 실제 패키지 경로에 맞게 수정해주세요.

import com.polyHub.notification.entity.Notification; // 실제 Notification 엔티티 경로에 맞게 수정해주세요.
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 클라이언트에게 알림 정보를 전달하기 위한 DTO
 */
@Getter
@Builder
public class NotificationResponseDto {

    private final Long id;
    private final String message;
    private final String link;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    /**
     * Notification 엔티티 객체를 NotificationResponseDto 객체로 변환하는 정적 팩토리 메소드입니다.
     * 이 메소드를 사용하면 서비스 코드에서 변환 로직을 깔끔하게 관리할 수 있습니다.
     * @param notification 변환할 Notification 엔티티 객체
     * @return 변환된 NotificationResponseDto 객체
     */
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .link(notification.getLink())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}