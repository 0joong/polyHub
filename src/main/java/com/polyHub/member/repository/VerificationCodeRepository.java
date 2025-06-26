package com.polyHub.member.repository;

import com.polyHub.member.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {
    // 필요하다면 findByEmail 등 메서드 추가 가능
}
