package com.polyHub.notification.service;

import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import com.polyHub.notification.entity.MemberFcmToken;
import com.polyHub.notification.repository.MemberFcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final MemberFcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveToken(String token, Long memberId) {
        // 1. 전달받은 token으로 DB에 이미 저장된 데이터가 있는지 확인합니다.
        Optional<MemberFcmToken> existingToken = fcmTokenRepository.findByToken(token);

        // 2. 토큰이 존재하지 않을 경우에만 새로 저장하는 로직을 수행합니다.
        if (existingToken.isEmpty()) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));

            MemberFcmToken newToken = MemberFcmToken.builder()
                    .member(member)
                    .token(token)
                    .build();
            newToken.setMember(member);
            newToken.setToken(token);
            // @CreatedDate 어노테이션을 사용하고 있다면 createdAt은 자동으로 설정됩니다.

            fcmTokenRepository.save(newToken);
            System.out.println("새로운 FCM 토큰이 저장되었습니다: " + token);
        } else {
            // 3. 토큰이 이미 존재하면 아무것도 하지 않거나, 로깅만 남깁니다.
            System.out.println("이미 등록된 FCM 토큰입니다. 저장을 건너뜁니다: " + token);
        }
    }

    @Transactional
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }
}