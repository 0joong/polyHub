package com.polyHub.notification.service;

import com.polyHub.member.entity.Member;
import com.polyHub.notification.dto.NotificationResponseDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {

    /**
     * 특정 사용자에게 알림을 보냅니다.
     * @param receiver 알림을 받을 사용자 엔티티
     * @param message  알림 내용
     * @param link     알림 클릭 시 이동할 URL
     */
    void send(Member receiver, String message, String link);

    /**
     * [추가] 클라이언트가 실시간 알림을 구독하기 위해 연결합니다.
     * @param memberId 구독하는 사용자의 ID
     * @return SseEmitter 객체 (서버-클라이언트 간의 실시간 연결 통로)
     */
    SseEmitter subscribe(Long memberId);


    List<NotificationResponseDto> findUnreadNotificationsByMember(Long memberId);

    void deleteNotification(Long notificationId, Long memberId);
}
