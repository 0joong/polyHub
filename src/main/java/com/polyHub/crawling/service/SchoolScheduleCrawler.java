package com.polyHub.crawling.service;

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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;

public class SchoolScheduleCrawler {

    public static void main(String[] args) {
        // 1. WebDriver 경로 설정 및 옵션 지정
        System.setProperty("webdriver.chrome.driver", "D:/project/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();

        // 최종 실행을 위해 Headless 모드를 활성화합니다.
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            String schoolUrl = "https://www.kopo.ac.kr/seongnam/content.do?menu=4277";
            driver.get(schoolUrl);

            System.out.println("1단계: '과정 전체' 링크를 클릭합니다...");
            WebElement courseMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.label-box[title='과정 선택']")));
            jsClick(driver, courseMenu);

            System.out.println("2단계: '2년제학위과정' 링크를 클릭합니다...");
            WebElement twoYearCourseLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-idx='11']")));
            jsClick(driver, twoYearCourseLink);

            System.out.println("3단계: '전체' 버튼을 클릭합니다...");
            WebElement allScheduleButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='year_txt_box']//a[span[text()='전체']]")));
            jsClick(driver, allScheduleButton);

            System.out.println("최종 일정 데이터가 로드되기를 기다립니다...");
            // [핵심 수정] 마지막 달력인 '12월'이 보일 때까지 기다립니다.
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='calendar_date']/span[@class='kr' and text()='12월']")));

            System.out.println("최종 HTML을 가져와서 일정 데이터를 추출합니다...");
            String finalHtml = driver.getPageSource();

            // Jsoup으로 HTML 파싱
            Document doc = Jsoup.parse(finalHtml);

            // 파일로 저장
            String filePath = "D:/project/crawled_schedule_data.txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {
                System.out.println("----------- 추출된 일정 데이터 -----------");

                // [핵심 수정] 월별 달력 박스를 모두 찾아서 순회합니다.
                Elements calendarBoxes = doc.select(".calendar_box");
                for (Element box : calendarBoxes) {
                    String month = box.select(".calendar_date .kr").text();
                    writer.println("===== " + month + " =====");
                    System.out.println("===== " + month + " =====");

                    Elements events = box.select(".cl_ev ul li");
                    for (Element event : events) {
                        // 날짜와 내용을 각각 추출합니다.
                        String date = event.select(".date").text();
                        String content = event.select(".event").text();

                        String schedule = "날짜: " + date + ", 내용: " + content;
                        System.out.println(schedule);
                        writer.println(schedule);
                    }
                    writer.println(); // 월별 구분을 위해 빈 줄 추가
                    System.out.println();
                }

                System.out.println("------------------------------------");
                System.out.println("추출된 일정 데이터가 아래 경로에 저장되었습니다.");
                System.out.println(filePath);
                System.out.println("------------------------------------");

            } catch (IOException e) {
                System.err.println("파일 저장 중 오류가 발생했습니다.");
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static void jsClick(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("arguments[0].click();", element);
    }
}
