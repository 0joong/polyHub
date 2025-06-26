package com.polyHub.member.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberVO {
    private Long id;

    private String email;
    private String password;
    private String name;
    private String phone;              // 선택 입력

    private boolean isVerified;        // 이메일 인증 여부
    private String verificationToken;  // 인증용 토큰
    private LocalDateTime tokenExpireAt; // 토큰 만료 시간

    private String faceId;             // 얼굴 벡터 ID
    private boolean faceRegistered;    // 얼굴 등록 여부

    private String provider;           // 로그인 방식 (local, google, etc.)
    private String providerId;         // 외부 로그인 고유 ID

    private LocalDateTime createdAt;   // 가입일시
}
