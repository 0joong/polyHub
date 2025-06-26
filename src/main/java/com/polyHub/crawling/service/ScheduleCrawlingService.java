package com.polyHub.crawling.service;

import com.polyHub.board.schedule.entity.Schedule;
import com.polyHub.board.schedule.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCrawlingService {

    private final ScheduleRepository scheduleRepository;

    @Transactional
    public void crawlAndSaveSchedules() {
        System.setProperty("webdriver.chrome.driver", "D:/project/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--window-size=1920,1080", "--disable-gpu", "--start-maximized", "--disable-popup-blocking", "--disable-dev-shm-usage", "--no-sandbox");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            navigateToFinalSchedule(driver, wait);

            log.info("최종 HTML을 가져와서 일정 데이터를 추출합니다...");
            String finalHtml = driver.getPageSource();
            List<Schedule> crawledSchedules = parseSchedulesFromHtml(finalHtml);

            // --- [핵심 수정] 지능형 동기화 로직 ---
            synchronizeSchedules(crawledSchedules);

        } catch (Exception e) {
            log.error("크롤링 중 오류가 발생했습니다.", e);
        } finally {
            driver.quit();
        }
    }

    /**
     * [추가] 크롤링된 데이터와 DB 데이터를 비교하여 동기화하는 메소드
     */
    private void synchronizeSchedules(List<Schedule> crawledSchedules) {
        if (crawledSchedules.isEmpty()) {
            log.warn("크롤링된 일정이 없어 DB 동기화를 건너뜁니다.");
            return;
        }

        // 1. 기존 DB 데이터를 Map으로 변환 (빠른 조회를 위해)
        Map<String, Schedule> dbScheduleMap = scheduleRepository.findAll().stream()
                .collect(Collectors.toMap(this::createScheduleIdentifier, schedule -> schedule));

        List<Schedule> schedulesToSave = new ArrayList<>();

        // 2. 크롤링된 데이터를 순회하며 DB와 비교
        for (Schedule crawled : crawledSchedules) {
            String identifier = createScheduleIdentifier(crawled);
            if (dbScheduleMap.containsKey(identifier)) {
                // 이미 DB에 존재하는 일정이면, 비교 대상에서 제거
                dbScheduleMap.remove(identifier);
            } else {
                // DB에 없는 새로운 일정이면, 저장 목록에 추가
                schedulesToSave.add(crawled);
            }
        }

        // 3. 맵에 남아있는 데이터는 웹사이트에서 사라진 것이므로 삭제 대상
        List<Schedule> schedulesToDelete = new ArrayList<>(dbScheduleMap.values());

        // 4. DB 작업 수행
        if (!schedulesToSave.isEmpty()) {
            log.info("새로운 일정 {}개를 저장합니다.", schedulesToSave.size());
            scheduleRepository.saveAll(schedulesToSave);
        }
        if (!schedulesToDelete.isEmpty()) {
            log.info("사라진 일정 {}개를 삭제합니다.", schedulesToDelete.size());
            scheduleRepository.deleteAll(schedulesToDelete);
        }
        if (schedulesToSave.isEmpty() && schedulesToDelete.isEmpty()) {
            log.info("데이터베이스에 변경된 내용이 없습니다.");
        } else {
            log.info("데이터베이스 동기화가 완료되었습니다.");
        }
    }

    /**
     * [추가] Schedule 객체의 고유 식별자를 생성하는 헬퍼 메소드
     */
    private String createScheduleIdentifier(Schedule schedule) {
        String endDateStr = schedule.getEndDate() != null ? schedule.getEndDate().toString() : "null";
        return schedule.getStartDate().toString() + ":" + endDateStr + ":" + schedule.getTitle();
    }


    private void navigateToFinalSchedule(WebDriver driver, WebDriverWait wait) {
        String schoolUrl = "https://www.kopo.ac.kr/seongnam/content.do?menu=4277";
        driver.get(schoolUrl);

        log.info("1단계: '과정 전체' 링크를 클릭합니다...");
        WebElement courseMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.label-box[title='과정 선택']")));
        jsClick(driver, courseMenu);

        log.info("2단계: '2년제학위과정' 링크를 클릭합니다...");
        WebElement twoYearCourseLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-idx='11']")));
        jsClick(driver, twoYearCourseLink);

        log.info("3단계: '전체' 버튼을 클릭합니다...");
        WebElement allScheduleButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='year_txt_box']//a[span[text()='전체']]")));
        jsClick(driver, allScheduleButton);

        log.info("최종 일정 데이터가 로드되기를 기다립니다...");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='calendar_date']/span[@class='kr' and text()='12월']")));
    }

    private List<Schedule> parseSchedulesFromHtml(String html) {
        Document doc = Jsoup.parse(html);
        Elements calendarBoxes = doc.select(".calendar_box");
        List<Schedule> schedules = new ArrayList<>();
        int currentYear = Integer.parseInt(doc.selectFirst(".year_box .year").text());

        Set<String> processedSchedules = new HashSet<>();

        for (Element box : calendarBoxes) {
            Elements events = box.select(".cl_ev ul li");
            for (Element event : events) {
                String dateStr = event.select(".date").text().trim();
                String contentStr = event.select(".event").text().trim();

                if (dateStr.isEmpty() || contentStr.isEmpty()) continue;

                String scheduleIdentifier = dateStr + "-" + contentStr;

                if (processedSchedules.add(scheduleIdentifier)) {
                    Schedule schedule = parseStringToScheduleEntity(dateStr, contentStr, currentYear);
                    if (schedule != null) {
                        schedules.add(schedule);
                    }
                }
            }
        }
        return schedules;
    }

    private Schedule parseStringToScheduleEntity(String dateStr, String contentStr, int year) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("MM-dd")
                .parseDefaulting(ChronoField.YEAR, year)
                .toFormatter();

        LocalDate startDate, endDate = null;

        try {
            if (dateStr.contains("~")) {
                String[] dates = dateStr.split("~");
                startDate = LocalDate.parse(dates[0].trim(), formatter);
                endDate = LocalDate.parse(dates[1].trim(), formatter);

                if (endDate.isBefore(startDate)) {
                    endDate = endDate.plusYears(1);
                }
            } else {
                startDate = LocalDate.parse(dateStr.trim(), formatter);
            }

            return Schedule.builder()
                    .title(contentStr)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();
        } catch (Exception e) {
            log.error("날짜 파싱 중 오류 발생: date='{}', content='{}'", dateStr, contentStr, e);
            return null;
        }
    }

    private static void jsClick(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }
}
