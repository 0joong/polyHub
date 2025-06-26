package com.polyHub.member.controller;

import com.polyHub.member.dto.PasswordChangeDto;
import com.polyHub.member.dto.RegisterDTO;
import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.member.entity.Member;
import com.polyHub.member.service.MemberService;
import com.polyHub.member.service.MemberUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberUserDetailsService memberUserDetailsService;


    @GetMapping("/login")
    public String loginForm() { return "member/login"; }

    @GetMapping("/face-login")
    public String faceLoginForm() {
        return "member/face-login";
    }

    @GetMapping("/register")
    public String registerForm() { return "member/register"; }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDTO dto, Model model, HttpServletRequest request) {
        if (!isAllowedKopoEmail(dto.getEmail())) {
            model.addAttribute("msg", "학교 이메일만 가입할 수 있습니다.");
            return "member/register";
        }

        if (memberService.existsByEmail(dto.getEmail())) {
            model.addAttribute("msg", "이미 사용 중인 이메일입니다.");
            return "member/register";
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .name(dto.getName())
                .phone(dto.getPhone())
                .role("USER")
                .build();

        memberService.register(member);

        UserDetails userDetails = memberUserDetailsService.loadUserByUsername(member.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);

        // ✅ 세션에도 직접 넣어주기
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return "redirect:/member/register-success";
    }

    @GetMapping("/member/register-success")
    public String registerSuccess() {
        return "member/register-success";
    }

    @GetMapping("/member/face-register")
    public String faceRegisterForm() {
        return "member/face-register";
    }

    /**
     * [수정] 마이페이지 뷰를 보여주는 메소드
     */
    @GetMapping("/mypage")
    public String mypage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        // 1. DB를 다시 조회하는 대신, 로그인 정보(Principal)에서 직접 Member 객체를 가져옵니다.
        // 이 방법이 훨씬 효율적이고 안전합니다.
        Member member = userDetails.getMember(); // CustomUserDetails에 getMember()가 있다고 가정

        // 2. 모델에 'member'라는 이름으로 회원 정보를 담습니다.
        model.addAttribute("member", member);

        // 3. 'board/member/mypage.html' 뷰를 반환합니다.
        return "member/mypage";
    }

    @GetMapping("/find-id")
    public String findIdForm() {
        return "member/find-id";
    }

    @PostMapping("/find-id")
    public String findIdProcess(
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            Model model
    ) {
        String foundEmail = memberService.findEmailByNameAndPhone(name, phone);

        if (foundEmail != null) {
            // 아이디를 찾았을 경우, 모델에 담아서 전달
            model.addAttribute("foundId", foundEmail);
        } else {
            // 아이디를 찾지 못했을 경우, 에러 메시지를 전달
            model.addAttribute("error", "일치하는 회원 정보가 없습니다.");
        }

        // 다시 아이디 찾기 페이지를 보여줌 (결과가 함께 표시됨)
        return "member/find-id";
    }

    @GetMapping("/find-password")
    public String findPasswordForm() { return "member/find-password"; }

    // 비밀번호 찾기를 처리하는 메소드
    @PostMapping("/find-password")
    public String findPasswordProcess(
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes
    ) {
        boolean success = memberService.issueTemporaryPassword(email, name);

        if (success) {
            redirectAttributes.addFlashAttribute("success", "입력하신 이메일로 임시 비밀번호를 발송했습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "일치하는 회원 정보가 없습니다.");
        }

        return "redirect:/find-password"; // 다시 비밀번호 찾기 페이지로 리다이렉트
    }

    /**
     * [추가] 비밀번호 변경을 처리하는 메소드
     */
    @PostMapping("/mypage/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute PasswordChangeDto passwordDto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 서비스 로직 호출
            memberService.changePassword(userDetails.getMember().getUsername(), passwordDto);
            // 성공 시 메시지 전달
            redirectAttributes.addFlashAttribute("passwordSuccess", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            // 서비스에서 발생한 예외(에러 메시지)를 그대로 전달
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }

        // 마이페이지로 다시 리다이렉트
        return "redirect:/mypage#change-password";
    }

    /*
     * [삭제] 회원 탈퇴 전용 페이지 GET 메소드는 더 이상 필요 없으므로 삭제합니다.
     * @GetMapping("/mypage/withdraw")
     * public String withdrawForm() { ... }
     */

    /**
     * [추가] 회원 탈퇴를 처리하는 메소드
     */
    @PostMapping("/mypage/withdraw")
    public String withdrawProcess(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("password") String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            memberService.withdraw(userDetails.getMember().getEmail(), password);
            // 탈퇴 성공 시, 현재 세션을 무효화(로그아웃)하고 로그인 페이지로 이동
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();

            // [수정] 로그인 페이지 대신, 탈퇴 성공 페이지로 리다이렉트
            return "redirect:/withdraw-success";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("withdrawError", e.getMessage());
            // 실패 시 다시 마이페이지의 탈퇴 탭으로 이동
            return "redirect:/mypage#withdraw";
        }
    }

    /**
     * [추가] 회원 탈퇴 성공 페이지를 보여주는 메소드
     */
    @GetMapping("/withdraw-success")
    public String withdrawSuccessPage() {
        return "member/withdraw-success";
    }

    public boolean isAllowedKopoEmail(String email) {
        return email != null && (
                email.endsWith("@office.kopo.ac.kr") || email.endsWith("@gspace.kopo.ac.kr")
        );
    }
}
