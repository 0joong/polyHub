package com.polyHub.crawling.service;

import com.polyHub.crawling.dto.BookDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LibraryCrawlingService {

    private static final String TARGET_URL = "https://lib.kopo.ac.kr/front?LIB_CODE=P8759139";
    private static final String WEBDRIVER_PATH = "D:/project/chromedriver.exe";

    public List<BookDto> searchBooks(String query) {
        System.setProperty("webdriver.chrome.driver", WEBDRIVER_PATH);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--start-maximized", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        List<BookDto> allBooks = new ArrayList<>();

        try {
            log.info("도서관 페이지로 이동합니다: {}", TARGET_URL);
            driver.get(TARGET_URL);

            log.info("검색창에 '{}'를 입력합니다...", query);
            WebElement searchBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("scSearch")));
            searchBox.sendKeys(query);

            log.info("검색 버튼을 클릭합니다...");
            WebElement searchButton = driver.findElement(By.id("btnSearch"));
            jsClick(driver, searchButton);

            WebElement totalCountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span.tit01")));
            Matcher matcher = Pattern.compile("\\d+").matcher(totalCountElement.getText());
            if (matcher.find()) {
                log.info("총 검색 결과: {}건", matcher.group());
            }

            // --- [핵심 수정] 페이징 요소 존재 여부 확인 후 분기 처리 ---

            // 1. 페이지네이션(ul.pagination) 요소가 존재하는지 먼저 확인합니다.
            List<WebElement> pagination = driver.findElements(By.cssSelector("ul.pagination"));

            if (pagination.isEmpty()) {
                // 2. 페이지네이션이 없으면, 1페이지만 있는 것이므로 현재 페이지만 파싱하고 종료합니다.
                log.info("결과가 1페이지입니다. 현재 페이지만 수집합니다.");
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.board_list table.table-primary01")));
                String pageSource = driver.getPageSource();
                allBooks.addAll(parseBookData(pageSource));
            } else {
                // 3. 페이지네이션이 있으면, 이전과 동일한 페이징 루프를 실행합니다.
                while (true) {
                    WebElement activePageElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.pagination li.active a")));
                    int currentPageNum = Integer.parseInt(activePageElement.getText());
                    log.info("{} 페이지의 데이터를 수집합니다...", currentPageNum);

                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.board_list table.table-primary01")));
                    String pageSource = driver.getPageSource();
                    allBooks.addAll(parseBookData(pageSource));

                    String nextpageNumStr = String.valueOf(currentPageNum + 1);
                    List<WebElement> nextLinks = driver.findElements(By.xpath("//ul[contains(@class, 'pagination')]//a[text()='" + nextpageNumStr + "']"));

                    if (nextLinks.isEmpty()) {
                        log.info("다음 페이지({}) 링크를 찾을 수 없습니다. 크롤링을 종료합니다.", nextpageNumStr);
                        break;
                    }

                    log.info("{} 페이지로 이동합니다...", nextpageNumStr);
                    jsClick(driver, nextLinks.get(0));

                    wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("li.page-item.active a"), nextpageNumStr));
                }
            }

        } catch (Exception e) {
            log.error("도서관 크롤링 중 오류가 발생했습니다.", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        log.info("총 {}건의 도서 정보를 수집했습니다.", allBooks.size());
        return allBooks;
    }

    private List<BookDto> parseBookData(String pageSource) {
        List<BookDto> bookList = new ArrayList<>();
        Document doc = Jsoup.parse(pageSource);
        Elements rows = doc.select("div.board_list table.table-primary01 tbody tr");

        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() >= 6) {
                bookList.add(BookDto.builder()
                        .title(cols.get(1).text().trim())
                        .author(cols.get(2).text().trim())
                        .publisher(cols.get(3).text().trim())
                        .status(cols.get(5).text().trim())
                        .build());
            }
        }
        return bookList;
    }

    private void jsClick(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }
}
