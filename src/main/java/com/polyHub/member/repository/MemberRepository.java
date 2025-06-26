package com.polyHub.member.repository;

import com.polyHub.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    Optional<Member> findById(long id);
    Optional<Member> findByNameAndPhone(String name, String phone);

    /**
     * [추가] 이메일과 이름으로 회원을 조회합니다.
     * @param email 회원 이메일
     * @param name 회원 이름
     * @return 조회된 회원 정보 (Optional)
     */
    Optional<Member> findByEmailAndName(String email, String name);

    /**
     * [추가] 특정 시각 이후에 가입한 회원 수를 조회합니다.
     * @param dateTime 기준 시각
     * @return 회원 수
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * [추가] 특정 시각 이후에 마지막으로 활동한 회원 수를 조회합니다.
     * (Member 엔티티에 마지막 로그인/활동 시각을 기록하는 'lastLoginAt' 필드가 필요합니다.)
     * @param dateTime 기준 시각
     * @return 활성 회원 수
     */
    long countByLastLoginAtAfter(LocalDateTime dateTime);

    /**
     * [추가] 특정 역할을 가진 모든 회원을 조회합니다.
     * @param role 조회할 역할 이름
     * @return 해당 역할을 가진 회원 목록
     */
    List<Member> findByRole(String role);
}
