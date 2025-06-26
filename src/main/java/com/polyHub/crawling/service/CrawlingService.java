package com.polyHub.crawling.service;

import com.polyHub.crawling.dto.NoticeDto;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTable;
import org.htmlunit.html.HtmlTableCell;
import org.htmlunit.html.HtmlTableRow;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.List;

public interface CrawlingService {


    @Scheduled(cron = "0 0/30 * * * *")
    void updateMenuCache() throws Exception;

    String getTodayMenu() throws Exception;

    String extractDate(String html);

    String extractDayOfWeek(String html);

    String extractMenuList(HtmlTableCell cell);

    /**
     * 주기적으로 공지사항을 크롤링하여 DB 및 Redis 캐시를 업데이트합니다.
     */
    @Scheduled(cron = "0 0/10 * * * *")
    void updateNoticeCache() throws Exception;

    /**
     * 최신 공지사항 목록을 가져옵니다.
     * @return 공지사항 DTO 리스트
     */
    List<NoticeDto> getNotices() throws Exception;
}
