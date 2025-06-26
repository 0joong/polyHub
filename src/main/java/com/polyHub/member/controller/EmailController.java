package com.polyHub.member.controller;

import com.polyHub.member.entity.VerificationCode;
import com.polyHub.member.repository.VerificationCodeRepository;
import com.polyHub.member.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class EmailController {

    private final EmailService emailService;
    private final VerificationCodeRepository codeRepository;

    @GetMapping("/sendVerificationCode")
    public Map<String, Object> sendCode(@RequestParam String email) {
        boolean success = emailService.sendVerificationCode(email);
        return Map.of("success", success);
    }

    @GetMapping("/verifyCode")
    public Map<String, Object> verifyCode(@RequestParam String email, @RequestParam String code) {
        VerificationCode verificationCode = codeRepository.findById(email).orElse(null);

        boolean success = false;
        if (verificationCode != null &&
                verificationCode.getCode().equals(code) &&
                verificationCode.getExpireTime().isAfter(LocalDateTime.now())) {
            success = true;
        }

        return Map.of("success", success);
    }
}
