package com.polyHub.member.entity;

import com.polyHub.board.free.entity.FreeBoardComment;
import com.polyHub.board.free.entity.FreeBoardPost;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "member") // 실제 테이블명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_id_seq")
    @SequenceGenerator(name = "member_id_seq", sequenceName = "MEMBER_ID_SEQ", allocationSize = 1)
    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * [추가] 마지막 로그인 또는 활동 시각을 기록하는 필드
     */
    private LocalDateTime lastLoginAt;

    // DB의 role 컬럼과 매핑될 필드
    private String role;

    // [핵심 1] 회원이 삭제될 때, 이 회원이 쓴 "게시글"도 함께 삭제합니다.
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeBoardPost> freeBoardPosts = new ArrayList<>();

    // [핵심 2] 댓글은 게시글을 통해 삭제되므로, 여기서는 직접적인 Cascade를 설정하지 않습니다.
    @OneToMany(mappedBy = "member")
    private List<FreeBoardComment> freeBoardComments = new ArrayList<>();

    /**
     * 사용자의 역할(role)에 따라 동적으로 권한 목록을 생성하여 반환합니다.
     * 이 메소드는 UserDetails 인터페이스의 일부이며, Spring Security가 사용자의 권한을 확인할 때 호출합니다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (this.role != null) {
            switch (this.role.toUpperCase()) {
                case "ADMIN":
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                case "MANAGER":
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                case "USER":
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
                default:
                    // 정의되지 않은 역할일 경우 기본 USER 권한 부여
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
            }
        } else {
            // 역할 정보가 없는 경우를 대비한 기본 권한
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email; // UserDetails에서 사용하는 username은 우리 시스템의 email입니다.
    }

    // --- UserDetails 인터페이스의 나머지 메소드 구현 ---
    // 계정의 만료, 잠김, 비번 만료, 활성화 상태를 나타냅니다.
    // 특별한 로직이 없다면 기본적으로 true를 반환하도록 설정합니다.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
