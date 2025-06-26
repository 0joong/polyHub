package com.polyHub.admin.service;

import com.polyHub.admin.dto.AdminMemberDto;
import com.polyHub.admin.dto.DashboardDto;
import com.polyHub.admin.dto.NotificationSendDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    /**
     * 대시보드 요약 정보를 조회합니다.
     * @return 대시보드 DTO
     */
    DashboardDto getDashboardSummary();

    /**
     * 모든 회원 목록을 페이징하여 조회합니다.
     * @param pageable 페이징 정보
     * @return 페이징된 회원 DTO 목록
     */
    Page<AdminMemberDto> getAllMembers(Pageable pageable);

    /**
     * [추가] 회원의 역할을 변경합니다.
     * @param memberId 대상 회원 ID
     * @param role 새로운 역할
     */
    void updateMemberRole(Long memberId, String role);

    /**
     * [추가] 회원을 강제로 탈퇴시킵니다.
     * @param memberId 대상 회원 ID
     */
    void deleteMember(Long memberId);

    /**
     * [추가] 지정된 대상에게 알림을 보냅니다.
     * @param sendDto 알림 정보 DTO
     */
    void sendNotificationToRole(NotificationSendDto sendDto);
}