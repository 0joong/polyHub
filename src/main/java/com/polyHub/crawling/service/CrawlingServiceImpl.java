package com.polyHub.crawling.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyHub.crawling.dto.NoticeDto;
import com.polyHub.crawling.entity.CrawledNotice;
import com.polyHub.crawling.repository.CrawledNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CrawlingServiceImpl implements CrawlingService {

    private final CacheService cacheService;
    private final CrawledNoticeRepository noticeRepository; // DB 저장을 위해 주입

    @Value("${lmstudio.api.noticeUrl}") // application.properties에 공지사항 URL 설정 필요
    private String noticeListUrl;

    // 내부에서 사용할 Enum: 크롤링 결과 상태를 나타냄
    private enum ProcessResult { NEW, DUPLICATE, INVALID }

    @Scheduled(cron = "0 0/30 * * * *")
    public void updateMenuCache() throws Exception {
        getTodayMenu();
        System.out.println("[스케줄러] 메뉴 갱신 완료!");
    }

    public String getTodayMenu() throws Exception {
        String cacheKey = "school:menu:today";
        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        StringBuilder result = new StringBuilder();
        result.append("학교 식단표\n\n");

        try (final WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            HtmlPage page = webClient.getPage("https://www.kopo.ac.kr/seongnam/content.do?menu=4304");
            webClient.waitForBackgroundJavaScript(2000);

            HtmlTable table = (HtmlTable) page.querySelector(".tbl_table.menu");
            if (table == null) {
                return "식단표를 찾을 수 없습니다.";
            }

            List<HtmlTableRow> rows = table.getBodies().getFirst().getRows();
            for (HtmlTableRow row : rows) {
                List<HtmlTableCell> cells = row.getCells();
                if (cells.size() < 4) continue;

                String dayHtml = cells.getFirst().asXml();
                String date = extractDate(dayHtml);
                String dayOfWeek = extractDayOfWeek(dayHtml);

                if ("토요일".equals(dayOfWeek) || "일요일".equals(dayOfWeek)) continue;

                result.append("날짜: ").append(date).append(" (").append(dayOfWeek).append(")\n");

                String breakfast = extractMenuList(cells.get(1));
                String lunch = extractMenuList(cells.get(2));
                String dinner = extractMenuList(cells.get(3));

                result.append(" 조식: ").append(breakfast).append("\n");
                result.append(" 중식: ").append(lunch).append("\n");
                result.append(" 석식: ").append(dinner).append("\n");
                result.append("---------------\n");
            }
        }

        // Redis에 10분간 캐시 저장
        cacheService.save(cacheKey, result.toString(), Duration.ofMinutes(10));

        return result.toString();
    }

    public String extractDate(String html) {
        int startIdx = html.indexOf("</script>") + "</script>".length();
        int endIdx = html.indexOf("<br");
        if (startIdx < endIdx) {
            return html.substring(startIdx, endIdx).trim();
        }
        return "날짜 없음";
    }

    public String extractDayOfWeek(String html) {
        int brIdx = html.indexOf("<br");
        if (brIdx != -1) {
            String dayPart = html.substring(brIdx + "<br/>".length()).replaceAll("<.*?>", "").trim();
            return dayPart.isEmpty() ? "요일 없음" : dayPart;
        }
        return "요일 없음";
    }

    public String extractMenuList(HtmlTableCell cell) {
        String cellText = cell.asNormalizedText();
        String[] items = cellText.split(",");
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            String menu = item.trim();
            if (!menu.isEmpty()) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(menu);
            }
        }
        return sb.toString().isEmpty() ? "없음" : sb.toString();
    }

    //----------------------------MENU END-----------------------------------------

    //----------------------------NOTICE START-------------------------------------

    @Override
    @Transactional
    @Scheduled(cron = "0 0/50 * * * *")
    public void updateNoticeCache() throws Exception {
        System.out.println("[스케줄러] 전체 공지사항 크롤링 및 DB 저장 시작...");
        crawlAndSaveAllNoticePages();
        System.out.println("[스케줄러] 공지사항 처리 완료.");
    }

    private void crawlAndSaveAllNoticePages() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setUseInsecureSSL(true);

            HtmlPage currentPage = webClient.getPage(noticeListUrl);
            final int MAX_PAGES_TO_CRAWL = 20;
            int pageCount = 1;

            while (pageCount <= MAX_PAGES_TO_CRAWL) {
                System.out.println("\n" + pageCount + " 페이지 크롤링 및 저장 중..."); // 가독성을 위해 개행 추가
                webClient.waitForBackgroundJavaScript(3000);

                HtmlTable table = currentPage.querySelector("table.tbl_list");
                if (table == null) {
                    // ===================== [디버깅 로그] =====================
                    System.out.println("[디버그] " + pageCount + " 페이지에서 'table.tbl_list' 테이블을 찾지 못했습니다. 크롤링을 중단합니다.");
                    // =========================================================
                    break;
                }

                for (final HtmlTableRow row : table.getBodies().get(0).getRows()) {
                    try {
                        processNoticeRow(row, webClient);
                    } catch (Exception e) {
                        System.err.println("개별 행 처리 중 예측하지 못한 오류 발생: " + row.asNormalizedText() + " | " + e.getMessage());
                    }
                }


                List<HtmlAnchor> nextLinks = currentPage.getByXPath("//a[contains(@class, 'btn_next') and not(contains(@class, 'mobile'))]");
                if (nextLinks.isEmpty()) {
                    System.out.println("마지막 페이지입니다. 크롤링을 종료합니다.");
                    break;
                }

                currentPage = nextLinks.get(0).click();
                pageCount++;
            }
        }
    }

    @Transactional
    public ProcessResult processNoticeRow(HtmlTableRow row, WebClient webClient) throws Exception {
        List<HtmlTableCell> cells = row.getCells();
        if (cells.isEmpty() || "notice".equals(cells.get(0).getAttribute("class")) || cells.size() < 4) {
            // 이 행은 공지사항이 아니므로 조용히 넘어갑니다.
            return ProcessResult.INVALID;
        }

        // ===================== [디버깅 로그] =====================
        System.out.println("[디버그] 행 처리 시작: " + row.asNormalizedText());
        // =========================================================

        HtmlAnchor linkElement = cells.get(1).querySelector("a");
        if (linkElement == null) {
            // ===================== [디버깅 로그] =====================
            System.out.println("[디버그] 제목(2번째 칸)에서 <a> 태그를 찾지 못했습니다.");
            // =========================================================
            return ProcessResult.INVALID;
        }

        // ===================== [디버깅 로그] =====================
        // onclick 속성의 실제 값을 확인하는 것이 매우 중요합니다.
        System.out.println("[디버그] <a> 태그의 onclick 속성: " + linkElement.getOnClickAttribute());
        // =========================================================

        String detailUrl = parseDetailUrl(linkElement);

        // ===================== [디버깅 로그] =====================
        System.out.println("[디버그] 파싱된 상세 URL: " + detailUrl);
        // =========================================================

        if ("#".equals(detailUrl)) {
            System.out.println("[디버그] URL 파싱 실패. 이 행을 건너뜁니다.");
            return ProcessResult.INVALID;
        }

        if (noticeRepository.existsByNoticeUrl(detailUrl)) {
            // ===================== [디버깅 로그] =====================
            System.out.println("[디버그] 이미 존재하는 URL입니다. DB 저장을 건너뜁니다: " + detailUrl);
            // =========================================================
            return ProcessResult.DUPLICATE;
        }

        // 이하 로직은 이전과 동일...
        String content = crawlContent(webClient, detailUrl);
        String fullTitleText = cells.get(1).asNormalizedText();
        String dateStr = cells.get(3).asNormalizedText().trim();
        LocalDate noticeDate;
        try {
            noticeDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            System.out.println("[디버그] 날짜 파싱 실패: " + dateStr);
            return ProcessResult.INVALID;
        }
        String category = cells.get(0).asNormalizedText();
        if (category.matches("\\d+")) {
            category = "일반";
        }

        CrawledNotice notice = CrawledNotice.builder()
                .category(category).title(fullTitleText).content(content)
                .noticeUrl(detailUrl).noticeDate(noticeDate)
                .build();

        // ===================== [디버깅 로그] =====================
        System.out.println("[디버그] DB 저장 시도: " + notice.getTitle());
        // =========================================================
        noticeRepository.save(notice);
        return ProcessResult.NEW;
    }

    // 상세 페이지 URL을 파싱하는 헬퍼 메소드
    private String parseDetailUrl(HtmlAnchor linkElement) {
        String onClickAttr = linkElement.getOnClickAttribute();
        Pattern pattern = Pattern.compile("\\('(\\d+)'\\)");
        Matcher matcher = pattern.matcher(onClickAttr);
        if (matcher.find()) {
            String postId = matcher.group(1);
            return String.format("https://www.kopo.ac.kr/seongnam/board.do?menu=4311&mode=view&post=%s", postId);
        }
        return "#";
    }

    // [수정] 상세 페이지의 본문 내용을 크롤링하는 헬퍼 메소드
    private String crawlContent(WebClient webClient, String detailUrl) {
        try {
            HtmlPage detailPage = webClient.getPage(detailUrl);
            webClient.waitForBackgroundJavaScript(2000);
            DomElement contentElement = detailPage.querySelector("td.txt_area");
            return (contentElement != null) ? contentElement.asNormalizedText() : "";
        } catch (Exception e) {
            System.err.println("상세 페이지(" + detailUrl + ") 내용 크롤링 중 오류: " + e.getMessage());
            return "";
        }
    }

    @Override
    public List<NoticeDto> getNotices() throws Exception {
        return new ArrayList<>(); // 현재 직접 사용되지 않음
    }
}
