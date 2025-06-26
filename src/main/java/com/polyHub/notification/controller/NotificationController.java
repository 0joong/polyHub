package com.polyHub.notification.controller;

import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.notification.dto.NotificationResponseDto;
import com.polyHub.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 로그인한 사용자가 실시간 알림을 구독하기 위해 연결하는 API입니다.
     * produces = "text/event-stream"은 이 연결이 SSE 프로토콜을 사용함을 명시합니다.
     *
     * @param userDetails 현재 로그인한 사용자의 상세 정보
     * @return SseEmitter 객체 (서버와 클라이언트 간의 실시간 연결 통로)
     */
    @GetMapping(value = "/api/notifications/subscribe", produces = "text/event-stream")
    @ResponseStatus(HttpStatus.OK)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 비로그인 사용자의 경우, 인증 필터에서 처리되므로 null 체크가 필요 없을 수 있지만,
        // 안전을 위해 한 번 더 확인합니다.
        if (userDetails == null) {
            return null;
        }
        return notificationService.subscribe(userDetails.getMember().getId());
    }

    @GetMapping("/api/notifications")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long memberId = userDetails.getMember().getId();
        List<NotificationResponseDto> unreadNotifications = notificationService.findUnreadNotificationsByMember(memberId);
        return ResponseEntity.ok(unreadNotifications);
    }

    // [추가] 알림 삭제 API
    @DeleteMapping("/api/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.deleteNotification(notificationId, userDetails.getMember().getId());
        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 응답
    }
}
