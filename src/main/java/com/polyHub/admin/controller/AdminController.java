package com.polyHub.admin.controller;

import com.polyHub.admin.dto.AdminMemberDto;
import com.polyHub.admin.dto.NotificationSendDto;
import com.polyHub.admin.service.AdminService;
import com.polyHub.member.entity.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest; // [추가] HttpServletRequest 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * [수정] 관리자 메인 페이지 요청을 처리합니다.
     * htmx 요청과 일반 요청을 구분하여 다른 뷰를 반환합니다.
     */
    @GetMapping
    public String adminPage(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            HttpServletRequest request // [추가] htmx 요청을 확인하기 위해 request 객체를 받습니다.
    ) {
        // 1. 서비스에서 필요한 데이터를 모두 가져옵니다. (기존과 동일)
        model.addAttribute("dashboard", adminService.getDashboardSummary());
        Page<AdminMemberDto> membersPage = adminService.getAllMembers(pageable);
        model.addAttribute("members", membersPage);

        // 2. 페이지네이션 계산 로직 (기존과 동일)
        int blockSize = 10;
        int startPage = (int) (Math.floor(membersPage.getNumber() / blockSize) * blockSize);
        int endPage = Math.min(startPage + blockSize - 1, membersPage.getTotalPages() - 1);
        if (endPage < 0) {
            endPage = 0;
        }
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        // ======================= [수정된 핵심 로직] =======================
        // htmx 라이브러리는 AJAX 요청 시 'HX-Request' 헤더를 true로 설정하여 보냅니다.
        // 이 헤더의 존재 여부로 htmx 요청인지 일반 브라우저 요청인지 구분합니다.
        String hxRequestHeader = request.getHeader("HX-Request");
        if ("true".equals(hxRequestHeader)) {
            // htmx를 통한 요청일 경우, 페이지 전체가 아닌 교체될 부분(fragment)만 반환합니다.
            return "admin/member_management :: memberManagementFragment";
        }
        // =================================================================

        // 일반적인 브라우저 요청일 경우, 전체 페이지를 담고 있는 main 템플릿을 반환합니다.
        return "admin/main";
    }

    /**
     * [수정 없음] 회원의 역할을 변경하는 요청을 처리합니다.
     */
    @PostMapping("/members/{id}/update-role")
    public String updateMemberRole(
            @PathVariable("id") Long memberId,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes
    ) {
        adminService.updateMemberRole(memberId, role);
        redirectAttributes.addFlashAttribute("managementSuccess", "회원 역할이 성공적으로 변경되었습니다.");
        // 해시에 #member-management가 아니라 #member_management 여야 할 수 있습니다. HTML의 id와 일치해야 합니다.
        return "redirect:/admin#member_management";
    }

    /**
     * [수정 없음] 회원을 강제 탈퇴시키는 요청을 처리합니다.
     */
    @PostMapping("/members/{id}/delete")
    public String deleteMember(
            @PathVariable("id") Long memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (currentUser.getMember().getId().equals(memberId)) {
            redirectAttributes.addFlashAttribute("managementError", "자신을 강제 탈퇴시킬 수 없습니다.");
            return "redirect:/admin#member_management";
        }
        adminService.deleteMember(memberId);
        redirectAttributes.addFlashAttribute("managementSuccess", "회원이 성공적으로 삭제되었습니다.");
        return "redirect:/admin#member_management";
    }

    /**
     * [수정 없음] 전체 알림 보내기 요청을 처리합니다.
     */
    @PostMapping("/send-notification")
    public String sendNotification(
            @ModelAttribute NotificationSendDto sendDto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminService.sendNotificationToRole(sendDto);
            redirectAttributes.addFlashAttribute("notificationSuccess", "알림이 성공적으로 발송 대기열에 추가되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("notificationError", "알림 발송 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
        return "redirect:/admin#notification";
    }
}
