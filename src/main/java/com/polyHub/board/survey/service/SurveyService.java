package com.polyHub.board.survey.service;

import com.polyHub.board.survey.dto.SurveyCreationDto;
import com.polyHub.board.survey.dto.SurveyDto;
import com.polyHub.board.survey.dto.SurveyResultDto;
import com.polyHub.board.survey.dto.SurveySubmissionDto;
import com.polyHub.board.survey.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface SurveyService {

    // 상태별 설문 목록 조회
    Page<SurveyDto> findSurveysByStatus(SurveyStatus status, Pageable pageable);

    // 설문 상세 조회 (참여용)
    SurveyDto findSurveyForParticipation(Long surveyId);

    /**
     * [수정] 설문 제출 처리 메소드 시그니처 변경
     * @param allParams 컨트롤러에서 받은 전체 폼 파라미터 맵
     * @param respondentId 현재 로그인한 사용자의 ID
     */
    public void submitSurvey(MultiValueMap<String, String> allParams, Long respondentId);

    /**
     * (관리자용) 새 설문을 생성합니다.
     * @param creationDto 화면에서 받은 설문 생성 정보
     * @param creator 현재 로그인한 사용자 이름
     * @return 생성된 설문의 ID
     */
    Long writeSurvey(SurveyCreationDto creationDto, String creator);

    /**
     * (관리자용) 설문을 삭제합니다.
     * @param surveyId 삭제할 설문의 ID
     */
    void deleteSurvey(Long surveyId);

    /**
     * 설문 결과를 조회합니다.
     * @param surveyId 결과를 조회할 설문의 ID
     * @return 통계 처리된 설문 결과 DTO
     */
    SurveyResultDto getSurveyResult(Long surveyId);

    // TODO: 필요 시 설문 수정을 위한 메소드 추가
    // void updateSurvey(Long surveyId, SurveyCreationDto creationDto);

    /**
     * [추가] 스케줄러에 의해 호출될 설문 상태 자동 업데이트 메소드
     */
    void updateSurveyStatus();

    /**
     * [추가] (관리자용) 설문을 강제로 마감합니다.
     * @param surveyId 마감할 설문의 ID
     */
    void closeSurvey(Long surveyId);
}
