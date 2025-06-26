package com.polyHub.controller;

import com.polyHub.crawling.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CrawlingService crawlingService;

    @GetMapping("/")
    public String home(Model model) {
        String menu;
        try {
            menu = crawlingService.getTodayMenu();
        } catch (Exception e) {
            // 크롤링 실패 시 기본 메시지 제공
            menu = "현재 식단표를 불러올 수 없습니다.";
            // 로그로 남기기
            System.out.println("식단표 크롤링 실패: " + e.getMessage());
        }
        model.addAttribute("menu", menu);
        return "index";
    }

    @GetMapping("/chat")
    public String Chat() {
        return "chat/chat";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "common/error/access-denied";
    }

    @GetMapping("/bookSearch")
    public String bookSearch() {
        return "board/book/bookSearch";
    }

}
