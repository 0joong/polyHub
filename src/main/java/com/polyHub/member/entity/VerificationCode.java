package com.polyHub.member.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {

    @Id
    private String email; // 이메일 주소를 PK로 사용 (또는 별도 ID로 해도 OK)

    private String code; // 인증 코드

    private LocalDateTime expireTime; // 유효 기간
}
