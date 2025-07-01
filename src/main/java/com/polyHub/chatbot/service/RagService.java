package com.polyHub.chatbot.service;

import com.polyHub.board.schedule.entity.Schedule;
import com.polyHub.board.schedule.repository.ScheduleRepository;
import com.polyHub.crawling.entity.CrawledNotice;
import com.polyHub.crawling.repository.CrawledNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RagService {

    private final CrawledNoticeRepository noticeRepository;
    private final ScheduleRepository scheduleRepository;

    public String findRelevantNotice(String userQuery) {
        // 1. 사용자의 질문을 공백 기준으로 키워드로 분리
        String[] keywords = userQuery.split("\\s+");
        if (keywords.length == 0) {
            return "관련 공지사항 없음";
        }

        // 2. 모든 키워드를 사용하여 DB에서 관련 공지사항 검색
        List<CrawledNotice> foundNotices = Stream.of(keywords)
                .flatMap(keyword -> {
                    if (keyword.length() < 2) return Stream.empty();
                    // 각 키워드로 제목 또는 내용 검색
                    return noticeRepository.findByTitleContainingOrContentContaining(keyword, keyword).stream();
                })
                .distinct() // 중복된 공지사항 제거
                .collect(Collectors.toList());

        // 3. 검색된 공지사항들 중에서 가장 관련도 높은 상위 2개를 선택
        return foundNotices.stream()
                .map(notice -> {
                    // 3-1. 각 공지사항의 관련도 점수 계산
                    int score = 0;
                    for (String keyword : keywords) {
                        if (notice.getTitle().contains(keyword)) score += 2; // 제목에 있으면 2점
                        if (notice.getContent().contains(keyword)) score += 1; // 내용에 있으면 1점
                    }
                    return new ScoredNotice(notice, score);
                })
                .filter(scored -> scored.score() > 0) // 점수가 0보다 큰 것만 필터링
                .sorted(Comparator.comparingInt(ScoredNotice::score).reversed()) // 점수 높은 순으로 정렬
                .limit(2) // 상위 2개만 선택
                .map(scored -> {
                    // 3-2. LLM에게 전달할 참고자료 형식으로 변환
                    CrawledNotice notice = scored.notice();
                    return String.format("공지 제목: %s, 내용: %s, 링크: %s",
                            notice.getTitle(),
                            notice.getContent(),
                            notice.getNoticeUrl());
                })
                .collect(Collectors.joining("\n---\n")); // 여러 개인 경우 구분선으로 연결
    }

    // [추가 시작] 일정 검색을 위한 메소드
    public String findRelevantSchedule(String userQuery) {
        String[] keywords = userQuery.split("\\s+");
        if (keywords.length == 0) {
            return "관련 일정 없음";
        }

        // 1. 키워드로 DB에서 관련 일정 검색
        List<Schedule> foundSchedules = Stream.of(keywords)
                .flatMap(keyword -> {
                    if (keyword.length() < 2) return Stream.empty();
                    // 제목 또는 내용으로 검색
                    return scheduleRepository.findByTitleContainingOrContentContaining(keyword, keyword).stream();
                })
                .distinct() // 중복된 일정 제거
                .collect(Collectors.toList());

        // 2. 검색된 일정들 중에서 가장 관련도 높은 상위 2개를 선택
        return foundSchedules.stream()
                .map(schedule -> {
                    // 2-1. 관련도 점수 계산
                    int score = 0;
                    for (String keyword : keywords) {
                        if (schedule.getTitle().contains(keyword)) score += 2; // 제목에 있으면 2점
                        if (schedule.getContent() != null && schedule.getContent().contains(keyword)) score += 1; // 내용에 있으면 1점
                    }
                    return new ScoredSchedule(schedule, score);
                })
                .filter(scored -> scored.score() > 0) // 점수가 0보다 큰 것만 필터링
                .sorted(Comparator.comparingInt(ScoredSchedule::score).reversed()) // 점수 높은 순으로 정렬
                .limit(2) // 상위 2개만 선택
                .map(scored -> {
                    // 2-2. LLM에게 전달할 참고자료 형식으로 변환
                    Schedule schedule = scored.schedule();
                    String startDate = schedule.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String endDate = (schedule.getEndDate() != null) ? schedule.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : startDate;
                    String content = (schedule.getContent() != null && !schedule.getContent().isEmpty()) ? schedule.getContent() : "상세 내용 없음";

                    return String.format("일정 제목: %s, 기간: %s ~ %s, 내용: %s",
                            schedule.getTitle(),
                            startDate,
                            endDate,
                            content);
                })
                .collect(Collectors.joining("\n---\n")); // 여러 개인 경우 구분선으로 연결
    }
    // [추가 끝]

    private record ScoredNotice(CrawledNotice notice, int score) {}
    private record ScoredSchedule(Schedule schedule, int score) {} // [추가] 일정 점수 기록을 위한 record

}