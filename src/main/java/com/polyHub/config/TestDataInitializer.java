package com.polyHub.config;

import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Profile("dev") // 'dev' 프로파일에서만 이 컴포넌트가 활성화됩니다.
public class TestDataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // [추가] 생성할 추가 테스트 계정의 수
    private static final int ADDITIONAL_USER_COUNT = 20;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- [개발용] 테스트 계정 데이터 초기화 시작 ---");
        System.out.println("--- 모든 테스트 계정의 비밀번호는 '1234' 입니다. ---");

        // 1. 주요 역할 계정 생성 (기존과 동일)
        createFixedAccount("user@example.com", "김학생", "01011111111", "USER");
        createFixedAccount("manager@example.com", "이매니저", "01022222222", "MANAGER");
        createFixedAccount("admin@example.com", "박관리", "01033333333", "ADMIN");

        // 2. [추가] 반복문을 사용하여 추가 USER 계정 생성
        for (int i = 1; i <= ADDITIONAL_USER_COUNT; i++) {
            String email = "testuser" + i + "@office.kopo.ac.kr";
            if (memberRepository.findByEmail(email).isEmpty()) {

                // 랜덤 전화번호 생성 (010-XXXX-XXXX 형식)
                long randomNum = ThreadLocalRandom.current().nextLong(10000000, 100000000);
                String phone = "010" + randomNum;

                Member testUser = Member.builder()
                        .email(email)
                        .password(passwordEncoder.encode("1234"))
                        .name("테스트유저" + i)
                        .phone(phone)
                        .role("USER")
                        .createdAt(LocalDateTime.now())
                        .build();
                memberRepository.save(testUser);
                System.out.println("- '" + email + "' (USER) 계정 생성 완료");
            }
        }

        System.out.println("--- 테스트 계정 데이터 초기화 완료 ---");
    }

    // 중복 코드를 줄이기 위한 헬퍼 메소드
    private void createFixedAccount(String email, String name, String phone, String role) {
        if (memberRepository.findByEmail(email).isEmpty()) {
            Member member = Member.builder()
                    .email(email)
                    .password(passwordEncoder.encode("1234"))
                    .name(name)
                    .phone(phone)
                    .role(role)
                    .createdAt(LocalDateTime.now())
                    .build();
            memberRepository.save(member);
            System.out.println("- '" + email + "' (" + role + ") 계정 생성 완료");
        }
    }
}
