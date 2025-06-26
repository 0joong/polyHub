package com.polyHub.config.handler;

import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.member.entity.Member;
import com.polyHub.member.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component // 이 클래스를 스프링 Bean으로 등록
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberService memberService;

    // [수정] @RequiredArgsConstructor를 제거하고, 생성자에 @Lazy를 직접 명시합니다.
    public CustomLoginSuccessHandler(@Lazy MemberService memberService) {
        this.memberService = memberService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Principal 객체를 가져옵니다. 이것은 우리가 만든 CustomUserDetails의 인스턴스입니다.
        Object principal = authentication.getPrincipal();

        // 2. 객체 타입을 확인하고, 안전하게 형 변환합니다.
        if (principal instanceof CustomUserDetails userDetails) {

            // 3. CustomUserDetails에서 실제 Member 객체를 가져옵니다.
            Member member = userDetails.getMember();

            // 4. Member 객체의 이메일(사용자 ID)을 사용하여 마지막 로그인 시각을 업데이트합니다.
            memberService.updateLastLoginAt(member.getEmail());
        }

        // 5. 기본 로그인 성공 URL("/")로 리다이렉트합니다.
        response.sendRedirect("/");
    }
}