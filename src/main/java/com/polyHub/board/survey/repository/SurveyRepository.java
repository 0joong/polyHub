package com.polyHub.board.survey.repository;

import com.polyHub.board.survey.entity.Survey;
import com.polyHub.board.survey.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    /**
     * 특정 상태(status)의 설문 목록을 페이징하여 조회합니다.
     * @param status 설문의 상태 (ACTIVE, CLOSED 등)
     * @param pageable 페이징 정보
     * @return 상태에 맞는 설문 페이지
     */
    Page<Survey> findByStatus(SurveyStatus status, Pageable pageable);

    /**
     * [수정] ID로 Survey를 조회할 때, questions 컬렉션만 함께 가져옵니다.
     * options 컬렉션은 Batch Fetching으로 처리합니다.
     */
    @Query("SELECT s FROM Survey s " +
            "LEFT JOIN FETCH s.questions " +
            "WHERE s.id = :id")
    Optional<Survey> findByIdWithDetails(@Param("id") Long id);

    /**
     * [추가] 'READY' 상태이면서 시작일이 오늘이거나 과거인 설문을 'ACTIVE'로 변경합니다.
     * @param today 현재 날짜
     * @return 변경된 row 수
     */
    @Modifying
    @Query("UPDATE Survey s SET s.status = 'ACTIVE' WHERE s.status = 'READY' AND s.startDate <= :today")
    int updateReadyToActive(@Param("today") LocalDate today);

    /**
     * [추가] 'ACTIVE' 상태이면서 종료일이 어제이거나 그 이전인 설문을 'CLOSED'로 변경합니다.
     * @param today 현재 날짜
     * @return 변경된 row 수
     */
    @Modifying
    @Query("UPDATE Survey s SET s.status = 'CLOSED' WHERE s.status = 'ACTIVE' AND s.endDate < :today")
    int updateActiveToClosed(@Param("today") LocalDate today);
}