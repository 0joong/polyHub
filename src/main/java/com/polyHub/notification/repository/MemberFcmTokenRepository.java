package com.polyHub.notification.repository;

import com.polyHub.notification.entity.MemberFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberFcmTokenRepository extends JpaRepository<MemberFcmToken, Long> {
    List<MemberFcmToken> findByMemberId(Long memberId);
    void deleteByToken(String token);
    // 토큰 값으로 데이터를 찾기 위한 메소드 추가
    Optional<MemberFcmToken> findByToken(String token);
}
