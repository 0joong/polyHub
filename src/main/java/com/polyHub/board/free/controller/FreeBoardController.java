package com.polyHub.board.free.controller;

import com.polyHub.board.free.dto.FreeBoardDetailDto;
import com.polyHub.board.free.dto.FreeBoardListDto;
import com.polyHub.board.free.dto.FreeBoardWriteDto;
import com.polyHub.board.free.service.FreeBoardService;
import com.polyHub.member.entity.CustomUserDetails;
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

@Controller
@RequestMapping("/free")
@RequiredArgsConstructor
public class FreeBoardController {

    private final FreeBoardService freeBoardService;

    /**
     * [수정] 게시글 목록 페이지에 검색 파라미터를 추가합니다.
     */
    @GetMapping
    public String list(
            @PageableDefault(size=10, sort="id", direction=Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "keyword", required = false) String keyword, // [추가]
            Model model
    ) {
        // [수정] 서비스 호출 시 keyword 전달
        Page<FreeBoardListDto> posts = freeBoardService.findPosts(pageable, keyword);
        model.addAttribute("posts", posts);

        // [추가] 뷰에서 검색어를 계속 사용(검색창에 값 유지, 페이지네이션 링크에 포함)하도록 모델에 추가
        model.addAttribute("keyword", keyword);

        return "board/free/list";
    }

    // 게시글 상세 보기 페이지
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long memberId = (userDetails != null) ? userDetails.getMember().getId() : null;
        FreeBoardDetailDto post = freeBoardService.findPostById(id, memberId);
        model.addAttribute("post", post);
        return "board/free/detail";
    }

    // 글쓰기 폼 페이지
    @GetMapping("/write")
    public String writeForm() {
        return "board/free/write";
    }

    // 글쓰기 처리
    @PostMapping("/write")
    public String write(@ModelAttribute FreeBoardWriteDto writeDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long newPostId = freeBoardService.writePost(writeDto, userDetails.getMember().getId());
        return "redirect:/free/" + newPostId;
    }

    /**
     * [추가] 게시글 수정 폼 페이지
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        FreeBoardDetailDto post = freeBoardService.findPostById(id, userDetails.getMember().getId());

        // 작성자 본인 확인
        if (!post.getWriter().equals(userDetails.getMember().getName())) {
            // 권한 없는 경우의 처리 (예: 에러 페이지로 리다이렉트)
            return "redirect:/error/403";
        }

        model.addAttribute("post", post);
        return "board/free/edit";
    }

    /**
     * [추가] 게시글 수정 처리
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(
            @PathVariable Long id,
            @ModelAttribute FreeBoardWriteDto updateDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        freeBoardService.updatePost(id, updateDto, userDetails.getMember().getId());
        return "redirect:/free/" + id; // 수정 후 상세 페이지로 이동
    }

    /**
     * [추가] 게시글 삭제 처리
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        freeBoardService.deletePost(id, userDetails.getMember().getId());
        return "redirect:/free"; // 삭제 후 목록 페이지로 이동
    }
}