package com.polyHub.board.survey.dto;

import lombok.Data;
import java.util.Map;

// 설문 제출 시 사용될 DTO
@Data
public class SurveySubmissionDto {
    private Long surveyId;
    private Long respondentId; // 현재 로그인한 사용자 ID
    // Key: 질문 ID, Value: 답변 (객관식은 선택지 ID, 주관식은 텍스트)
    private Map<Long, String> answers;
}
