package com.polyHub.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.polyHub.member.entity.Member;
import com.polyHub.notification.dto.NotificationResponseDto;
import com.polyHub.notification.entity.MemberFcmToken;
import com.polyHub.notification.entity.Notification;
import com.polyHub.notification.repository.MemberFcmTokenRepository;
import com.polyHub.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.lettuce.core.RedisURI.DEFAULT_TIMEOUT;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;
    private final MemberFcmTokenRepository fcmTokenRepository;
    private final ObjectMapper objectMapper;
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 3600000L; // 1시간

    @Override
    @Transactional
    public void send(Member receiver, String message, String link) {
        // 1. 알림 기록을 DB에 저장합니다. (오프라인 사용자를 위해)
        com.polyHub.notification.entity.Notification notification = com.polyHub.notification.entity.Notification.builder()
                .receiver(receiver)
                .message(message)
                .link(link)
                .build();
        notificationRepository.save(notification);

        // 2. 해당 사용자의 모든 FCM 기기 토큰을 조회합니다.
        List<MemberFcmToken> tokens = fcmTokenRepository.findByMemberId(receiver.getId());
        if (tokens.isEmpty()) {
            log.warn("알림을 보낼 FCM 토큰이 없습니다. memberId={}", receiver.getId());
            return;
        }

        // 3. 각 기기(토큰)에 푸시 알림을 보냅니다.
        for (MemberFcmToken token : tokens) {
            // [수정] FCM 메시지 구성 시, Firebase Notification 클래스의 전체 경로를 사용합니다.
            com.google.firebase.messaging.Notification fcmNotification = com.google.firebase.messaging.Notification.builder()
                    .setTitle("PolyAI HUB 새 알림")
                    .setBody(message)
                    .build();

            Message fcmMessage = Message.builder()
                    .setToken(token.getToken())
                    .setNotification(fcmNotification)
                    .putData("link", link)
                    // [중요] 삭제 기능을 위해 DB에 저장된 알림 ID를 함께 보냅니다.
                    .putData("notificationId", String.valueOf(notification.getId()))
                    .build();


            try {
                // Firebase에 메시지 전송 요청
                firebaseMessaging.send(fcmMessage);
                log.info("FCM 알림 발송 성공: memberId={}, token={}", receiver.getId(), token.getToken());
            } catch (Exception e) {
                log.error("FCM 알림 발송 실패: token={}", token.getToken(), e);
                // TODO: 전송에 실패한 토큰은 DB에서 삭제하는 로직 추가 가능
            }
        }
    }

    /**
     * [추가] 클라이언트가 알림을 구독하는 로직
     */
    @Override
    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        sseEmitters.put(memberId, emitter);
        log.info("SSE: 새로운 연결 생성 [memberId={}, 현재 연결 수={}]", memberId, sseEmitters.size());

        // 연결이 어떤 이유로든 종료될 때 (완료, 타임아웃, 에러) emitter를 제거합니다.
        emitter.onCompletion(() -> sseEmitters.remove(memberId));
        emitter.onTimeout(() -> sseEmitters.remove(memberId));
        emitter.onError(e -> sseEmitters.remove(memberId));

        // 연결 직후, 503 오류 방지를 위한 더미 이벤트 전송
        sendToClient(memberId, "connect", "Connection established.");
        return emitter;
    }

    /**
     * 특정 클라이언트에게 데이터를 전송하는 헬퍼 메소드
     */
    private void sendToClient(Long memberId, String eventName, Object data) {
        SseEmitter emitter = sseEmitters.get(memberId);
        if (emitter != null) {
            try {
                String jsonData = objectMapper.writeValueAsString(data);
                emitter.send(SseEmitter.event().name(eventName).data(jsonData));
                log.info("SSE: 데이터 전송 성공 [memberId={}, event={}]", memberId, eventName);
            } catch (IOException e) {
                sseEmitters.remove(memberId);
                log.error("SSE: 데이터 전송 중 클라이언트 연결 오류, Emitter 제거 [memberId={}]", memberId);
            }
        } else {
            log.warn("SSE: 알림 발송 대상 없음 (Emitter not found) [memberId={}]", memberId);
        }
    }

    /**
     * [확인할 부분] 이 메소드가 클래스 안에 정확히 포함되어 있는지 확인해주세요.
     * 특정 회원의 읽지 않은 모든 알림을 조회하여 DTO 리스트로 반환합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 읽지 않은 알림의 DTO 리스트
     */
    @Override
    public List<NotificationResponseDto> findUnreadNotificationsByMember(Long memberId) {
        // 1. Repository를 사용하여 데이터베이스에서 알림 목록을 가져옵니다.
        List<com.polyHub.notification.entity.Notification> notifications = notificationRepository.findByReceiver_IdAndIsReadFalseOrderByCreatedAtDesc(memberId);

        // 2. 가져온 엔티티 리스트를 DTO 리스트로 변환합니다.
        return notifications.stream()
                .map(NotificationResponseDto::from) // DTO의 정적 팩토리 메소드 사용
                .collect(Collectors.toList());
    }
    // [추가] 알림 삭제 서비스 메소드
    @Override
    public void deleteNotification(Long notificationId, Long memberId) {
        // 알림 ID로 알림을 찾습니다. 없으면 예외를 발생시킵니다.
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다. ID: " + notificationId));

        // 알림의 수신자 ID와 현재 로그인한 사용자의 ID가 일치하는지 확인합니다.
        if (!notification.getReceiver().getId().equals(memberId)) {
            // 일치하지 않으면 권한 없음 예외를 발생시킵니다.
            throw new AccessDeniedException("해당 알림을 삭제할 권한이 없습니다.");
        }

        // 소유권이 확인되면 알림을 삭제합니다.
        notificationRepository.delete(notification);
    }
}