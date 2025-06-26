package com.polyHub.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    // [수정] application.properties에서 URL을 주입받도록 변경
    private final String lmStudioUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // @Value 어노테이션을 사용하여 생성자를 통해 값을 주입합니다.
    public ChatbotService(@Value("${lmstudio.api.url}") String lmStudioUrl) {
        this.lmStudioUrl = lmStudioUrl;
    }

    public String getChatbotResponse(String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);


        return restTemplate.postForObject(lmStudioUrl, entity, String.class);
    }
}