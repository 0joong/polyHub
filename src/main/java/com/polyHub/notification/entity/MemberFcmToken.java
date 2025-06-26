package com.polyHub.notification.entity;

import com.polyHub.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "MEMBER_FCM_TOKEN")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자는 protected
@AllArgsConstructor
@Builder // 빌더 패턴을 사용하기 위한 어노테이션
public class MemberFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_FCM_TOKEN_SEQ_GEN")
    @SequenceGenerator(name = "MEMBER_FCM_TOKEN_SEQ_GEN", sequenceName = "MEMBER_FCM_TOKEN_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String token;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MemberFcmToken(Member member, String token) {
        this.member = member;
        this.token = token;
    }
}