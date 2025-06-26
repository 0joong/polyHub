package com.polyHub.member.service;

import com.polyHub.member.dto.PasswordChangeDto;
import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public void register(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }

    /**
     * [수정] 아이디(이메일)를 마스킹 없이 그대로 반환합니다.
     */
    @Override
    public String findEmailByNameAndPhone(String name, String phone) {
        return memberRepository.findByNameAndPhone(name, phone)
                .map(Member::getEmail) // 마스킹 로직을 제거하고 이메일을 바로 반환
                .orElse(null); // 일치하는 회원이 없으면 null 반환
    }

    /**
     * [추가] 임시 비밀번호 발급 로직
     */
    @Override
    @Transactional
    public boolean issueTemporaryPassword(String email, String name) {
        // 1. 이메일과 이름으로 사용자가 존재하는지 확인
        return memberRepository.findByEmailAndName(email, name)
                .map(member -> {
                    // 2. 임시 비밀번호 생성 (여기서는 8자리 UUID 사용)
                    String tempPassword = UUID.randomUUID().toString().substring(0, 8);

                    // 3. 생성된 임시 비밀번호를 암호화하여 DB에 업데이트
                    member.setPassword(passwordEncoder.encode(tempPassword));
                    memberRepository.save(member); // 변경된 내용 저장

                    // 4. 사용자 이메일로 임시 비밀번호 발송
                    sendEmail(member.getEmail(), "임시 비밀번호 안내", "회원님의 임시 비밀번호는 " + tempPassword + " 입니다.");

                    return true; // 성공
                })
                .orElse(false); // 일치하는 회원이 없으면 실패
    }

    // 이메일 발송 헬퍼 메소드
    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * [추가] 비밀번호 변경 로직
     */
    @Override
    @Transactional
    public void changePassword(String email, PasswordChangeDto passwordDto) {
        // 1. 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 2. 현재 사용자 정보를 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3. 현재 비밀번호가 올바른지 확인
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        // 4. 새 비밀번호를 암호화하여 DB에 저장
        member.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        memberRepository.save(member);
    }

    /**
     * [추가] 회원 탈퇴 로직
     */
    @Override
    @Transactional
    public void withdraw(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 2. DB에서 회원 정보 삭제
        // TODO: 회원이 작성한 게시글, 댓글 등 다른 데이터도 함께 처리하는 로직 추가 필요
        memberRepository.delete(member);
    }

    /**
     * [추가] 마지막 로그인 시각 업데이트 로직
     */
    @Override
    @Transactional
    public void updateLastLoginAt(String email) {
        // 이메일로 회원을 찾아, lastLoginAt 필드를 현재 시간으로 설정합니다.
        // @Transactional에 의해 메소드가 끝나면 변경된 내용이 자동으로 DB에 반영됩니다(더티 체킹).
        memberRepository.findByEmail(email).ifPresent(member -> {
            member.setLastLoginAt(LocalDateTime.now());
        });
    }
}
