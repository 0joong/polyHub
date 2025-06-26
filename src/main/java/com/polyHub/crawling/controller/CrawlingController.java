package com.polyHub.crawling.controller;

import com.polyHub.crawling.service.CrawlingService;
import com.polyHub.crawling.service.ScheduleCrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CrawlingController {

    private final CrawlingService crawlingService;
    private final ScheduleCrawlingService scheduleCrawlingService;

    /**
     * [수동 실행] 관리자가 직접 크롤링을 실행하기 위한 API 엔드포인트입니다.
     * 관리자(ADMIN) 권한이 있는 사용자만 호출할 수 있습니다.
     * @return 작업 시작 메시지
     */
    @PostMapping("/run-schedule")
    @PreAuthorize("hasRole('ADMIN')") // ADMIN 역할만 이 메소드를 호출할 수 있도록 보안 설정
    public ResponseEntity<String> runScheduleCrawling() {
        log.info("관리자에 의해 수동으로 학사일정 크롤링을 시작합니다.");

        // 비동기 실행을 위해 새 스레드에서 서비스 메소드 호출
        new Thread(scheduleCrawlingService::crawlAndSaveSchedules).start();

        return ResponseEntity.ok("학사일정 크롤링 작업이 시작되었습니다. 서버 로그를 확인하세요.");
    }

    /**
     * [자동 실행] 정해진 시간에 자동으로 크롤링을 실행하는 스케줄러입니다.
     * cron 표현식: "초 분 시 일 월 요일"
     * 아래 예시는 매일 새벽 4시에 실행됩니다.
     */
    // [수정] 테스트를 위해 매 1분마다 실행되도록 변경
    @Scheduled(cron = "0 * * * * ?") // 매 분 0초에 실행
    public void runScheduledCrawlingTask() {
        log.info("예약된 학사일정 자동 크롤링을 시작합니다.");
        scheduleCrawlingService.crawlAndSaveSchedules();
        log.info("예약된 학사일정 자동 크롤링이 완료되었습니다.");
    }

    @GetMapping("/api/menu")
    public String getMenu() throws Exception {
        return crawlingService.getTodayMenu();
    }
}
