package com.polyHub.chatbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyHub.chatbot.dto.ChatRequestDto;
import com.polyHub.chatbot.service.ChatbotService;
import com.polyHub.chatbot.service.RagService;
import com.polyHub.crawling.service.CrawlingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatbotProxyController {


    private final ChatbotService chatbotService;
    private final CrawlingService crawlingService;
    private final RagService ragService;
    private final String menuUrl;
    private final String noticeUrl;
    private final ObjectMapper objectMapper; // [추가] JSON 변환을 위한 ObjectMapper

    // [수정] 생성자에 ObjectMapper 추가
    public ChatbotProxyController(
            ChatbotService chatbotService,
            CrawlingService crawlingService,
            RagService ragService,
            @Value("${lmstudio.api.menuUrl}") String menuUrl,
            @Value("${lmstudio.api.noticeUrl}") String noticeUrl,
            ObjectMapper objectMapper
    ) {
        this.chatbotService = chatbotService;
        this.crawlingService = crawlingService;
        this.ragService = ragService;
        this.menuUrl = menuUrl;
        this.noticeUrl = noticeUrl;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/chatbot-proxy")
    public ResponseEntity<String> proxyChat(@RequestBody ChatRequestDto requestDto) {
        try {
            // 1. 마지막 사용자 메시지를 가져옵니다.
            String userQuery = requestDto.getMessages().getLast().get("content");

            // 2. RAG 서비스를 사용하여 관련 공지사항을 검색합니다.
            String relevantNotice = ragService.findRelevantNotice(userQuery);

            // 3. 식단표, 날짜 등 다른 정보도 가져옵니다.
            String todayMenu = crawlingService.getTodayMenu();
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

            // 4. 검색된 모든 정보를 종합하여 최종 시스템 프롬프트를 만듭니다.
            String systemContent = String.format(
                    "You are a helpful AI assistant named '폴리봇'. Your role is to assist students of the PolyAISW department." +
                            "Always answer in friendly Korean. Do not use markdown." +
                            "Based *only* on the context below, answer the user's question.\n" +
                            "---CONTEXT---\n" +
                            "Today's Date: %s\n" +
                            "Today's Menu: %s\n" +
                            "Relevant Announcement: %s\n" +
                            "General Announcements URL: %s\n" +
                            "--------------",
                    currentDate,
                    todayMenu.isEmpty() ? "정보 없음" : todayMenu,
                    relevantNotice.isEmpty() ? "관련 정보 없음" : relevantNotice,
                    noticeUrl
            );

            Map<String, String> contextMessage = new HashMap<>();
            contextMessage.put("role", "system");
            contextMessage.put("content", systemContent);

            requestDto.getMessages().addFirst(contextMessage);

            String requestBodyString = objectMapper.writeValueAsString(requestDto);
            String response = chatbotService.getChatbotResponse(requestBodyString);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("{\"error\": \"챗봇 서버와 통신 중 오류가 발생했습니다.\"}");
        }
    }
}