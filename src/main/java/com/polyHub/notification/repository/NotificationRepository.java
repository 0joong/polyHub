package com.polyHub.notification.repository;

import com.polyHub.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 사용자의 모든 알림을 최신순으로 조회
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long memberId);

    /**
     * Notification 엔티티의 'receiver' 필드(Member 타입) 안에 있는 'id' 필드를 기준으로 조회합니다.
     * IsRead가 False인 것을 찾고, 생성일자(createdAt) 내림차순으로 정렬합니다.
     */
    List<Notification> findByReceiver_IdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);
}