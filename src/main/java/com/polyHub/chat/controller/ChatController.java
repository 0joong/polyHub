package com.polyHub.chat.controller;

import com.polyHub.chat.dto.ChatMessageDto;
import com.polyHub.member.entity.CustomUserDetails;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage") // /app/chat.sendMessage
    @SendTo("/room/room1") // 구독자들에게 메시지 전달
    public ChatMessageDto sendMessage(ChatMessageDto message, Principal principal) {
        Authentication authentication = (Authentication) principal;
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String realName = userDetails.getName();
        message.setSender(realName);
        message.setSender(principal.getName());
        return message;
    }
}