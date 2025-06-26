package com.polyHub.notification.controller;

import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.notification.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/api/fcm/token")
    public ResponseEntity<Void> saveFcmToken(@RequestBody Map<String, String> payload,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        String token = payload.get("token");
        fcmTokenService.saveToken(token, userDetails.getMember().getId());
        return ResponseEntity.ok().build();
    }
}