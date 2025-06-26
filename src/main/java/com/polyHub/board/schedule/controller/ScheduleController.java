package com.polyHub.board.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleController {

    /**
     * '/schedule' GET 요청을 받으면 'schedule/schedule.html' 뷰를 반환합니다.
     * @return 뷰 이름
     */
    @GetMapping("/schedule")
    public String schedulePage() {
        return "schedule/schedule"; // templates/schedule/schedule.html 파일을 의미합니다.
    }
}