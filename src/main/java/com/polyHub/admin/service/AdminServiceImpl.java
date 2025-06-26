package com.polyHub.admin.service;

import com.polyHub.admin.dto.AdminMemberDto;
import com.polyHub.admin.dto.DashboardDto;
import com.polyHub.admin.dto.NotificationSendDto;
import com.polyHub.board.free.repository.FreeBoardCommentRepository;
import com.polyHub.board.free.repository.FreeBoardPostRepository;
import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import com.polyHub.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final MemberRepository memberRepository;
    private final FreeBoardPostRepository postRepository;
    private final FreeBoardCommentRepository commentRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher; // [수정] NotificationService 대신 주입

    @Override
    public DashboardDto getDashboardSummary() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);

        long newUsers = memberRepository.countByCreatedAtAfter(yesterday);
        long totalMembers = memberRepository.count();
        long activeUsers = memberRepository.countByLastLoginAtAfter(lastMonth);
        long newPosts = postRepository.countByCreatedAtAfter(yesterday);
        long newComments = commentRepository.countByCreatedAtAfter(yesterday);

        return DashboardDto.builder()
                .newUsersCount24h(newUsers)
                .totalMembersCount(totalMembers)
                .monthlyActiveUsersCount(activeUsers)
                .dailyPostsCount(newPosts)
                .dailyCommentsCount(newComments)
                .build();
    }

    @Override
    public Page<AdminMemberDto> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(AdminMemberDto::fromEntity);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        // Member 엔티티에 setRole이 있어야 합니다.
        member.setRole(role);
        // @Transactional에 의해 변경된 내용이 자동으로 DB에 반영됩니다(더티 체킹).
    }

    /**
     * [수정] deleteById 대신, findById로 먼저 조회한 후 delete하는 방식으로 변경
     */
    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        // 1. ID로 Member 엔티티를 먼저 조회합니다.
        Member memberToDelete = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 회원을 찾을 수 없습니다."));

        // 2. 조회된 엔티티 객체 자체를 사용하여 삭제를 수행합니다.
        memberRepository.delete(memberToDelete);
    }

    /**
     * 지정된 대상에게 알림을 보냅니다.
     */
    @Override
    @Transactional
    public void sendNotificationToRole(NotificationSendDto sendDto) {
        String targetRole = sendDto.getTargetRole();
        List<Member> targetMembers;

        if ("ALL".equalsIgnoreCase(targetRole)) {
            targetMembers = memberRepository.findAll();
        } else {
            targetMembers = memberRepository.findByRole(targetRole);
        }

        // NotificationService를 직접 호출하여 알림 발송
        for (Member member : targetMembers) {
            notificationService.send(member, sendDto.getMessage(), sendDto.getLink());
        }
    }
}
