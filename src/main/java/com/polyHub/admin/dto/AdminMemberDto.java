package com.polyHub.admin.dto;

import com.polyHub.member.entity.Member;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

// 회원 관리 목록에 보여줄 데이터를 담는 DTO
@Data
@Builder
public class AdminMemberDto {
    private Long id;
    private String email;
    private String name;
    private String role;
    private LocalDateTime createdAt;

    public static AdminMemberDto fromEntity(Member member) {
        return AdminMemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }
}