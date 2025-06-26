package com.polyHub.board.free.controller;

import com.polyHub.board.free.dto.ReactionDto;
import com.polyHub.board.free.entity.ReactionType;
import com.polyHub.board.free.service.FreeBoardService;
import com.polyHub.member.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class FreeBoardApiController {

    private final FreeBoardService freeBoardService;

    @PostMapping("/{postId}/react")
    public ResponseEntity<ReactionDto> reactToPost(
            @PathVariable Long postId,
            @RequestParam ReactionType reactionType,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        ReactionDto result = freeBoardService.reactToPost(postId, userDetails.getMember().getId(), reactionType);
        return ResponseEntity.ok(result);
    }
}