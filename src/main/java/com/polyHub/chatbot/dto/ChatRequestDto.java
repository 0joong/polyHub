package com.polyHub.chatbot.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequestDto {
    private String model;
    private List<Map<String, String>> messages;
    private double temperature;
}
