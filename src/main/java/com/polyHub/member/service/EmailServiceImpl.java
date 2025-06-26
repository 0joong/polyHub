// src/main/java/com/polyHub/member/service/impl/EmailServiceImpl.java
package com.polyHub.member.service;

import com.polyHub.member.service.EmailService;
import com.polyHub.member.entity.VerificationCode;
import com.polyHub.member.repository.VerificationCodeRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository codeRepository;

    @Override
    public boolean sendVerificationCode(String email) {
        String code = generateRandomCode();

        // DB에 코드와 이메일 매핑 저장
        VerificationCode verification = new VerificationCode(email, code, LocalDateTime.now().plusMinutes(10));
        codeRepository.save(verification);

        // 이메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[PolyAI] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n10분 내에 입력해주세요.");
        mailSender.send(message);

        return true;
    }

    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 랜덤 숫자
    }
}
