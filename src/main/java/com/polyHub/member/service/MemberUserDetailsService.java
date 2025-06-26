package com.polyHub.member.service;

import com.polyHub.member.entity.CustomUserDetails;
import com.polyHub.member.entity.Member;
import com.polyHub.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. DB에서 이메일로 Member 정보를 가져옵니다.
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // 2. 권한 목록을 담을 빈 리스트를 생성합니다.
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 3. DB에서 가져온 Member의 role 문자열을 가져옵니다.
        String role = member.getRole(); // getRole() 메소드가 Member 엔티티에 있다고 가정합니다.

        // 4. role 문자열에 따라 계층적인 권한을 부여합니다.
        //    (예: ADMIN은 MANAGER와 USER 권한을 모두 포함)
        if (role != null) {
            switch (role.toUpperCase()) {
                case "ADMIN":
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                case "MANAGER":
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                case "USER":
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
                default:
                    // 정의되지 않은 role일 경우 기본 권한 부여
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
            }
        } else {
            // role 필드가 null인 경우를 대비한 기본 권한
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }


        // 5. Member 정보와 동적으로 생성된 권한 목록을 CustomUserDetails에 담아 반환합니다.
        return new CustomUserDetails(member, authorities);
    }
}


