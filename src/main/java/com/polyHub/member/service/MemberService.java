package com.polyHub.member.service;

import com.polyHub.member.dto.PasswordChangeDto;
import com.polyHub.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
public interface MemberService {

    Member findByEmail(String email);
    void register(Member member);
    boolean existsByEmail(String email);
    String findEmailByNameAndPhone(String name, String phone);
    String encodePassword(String rawPassword);
    /**
     * [추가] 임시 비밀번호를 발급하고 이메일로 전송합니다.
     * @param email 회원 이메일
     * @param name 회원 이름
     * @return 성공 여부 (true/false)
     */
    boolean issueTemporaryPassword(String email, String name);

    /**
     * [추가] 사용자의 비밀번호를 변경합니다.
     * @param email 사용자의 이메일
     * @param passwordDto 변경할 비밀번호 정보 DTO
     */
    void changePassword(String email, PasswordChangeDto passwordDto);

    /**
     * [추가] 회원 탈퇴를 처리합니다.
     * @param email 탈퇴할 사용자의 이메일
     * @param password 본인 확인용 비밀번호
     */
    void withdraw(String email, String password);

    /**
     * [추가] 사용자의 마지막 로그인 시각을 현재로 업데이트합니다.
     * @param email 업데이트할 사용자의 이메일
     */
    void updateLastLoginAt(String email);
}
