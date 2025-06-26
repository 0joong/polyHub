package com.polyHub.board.free.controller;

import com.polyHub.board.free.dto.CommentDto;
import com.polyHub.board.free.dto.CommentWriteDto;
import com.polyHub.board.free.service.FreeBoardService;
import com.polyHub.member.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class FreeBoardCommentController {

    private final FreeBoardService freeBoardService;

    @PostMapping
    public ResponseEntity<CommentDto> writeComment(
            @RequestBody CommentWriteDto writeDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        CommentDto result = freeBoardService.writeComment(writeDto, userDetails.getMember().getId());
        return ResponseEntity.ok(result);
    }
}