package com.polyHub.board.survey.dto;

import com.polyHub.board.survey.enums.SurveyStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

// 목록, 상세 보기 등에 사용될 DTO
@Data
@Builder
public class SurveyDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private SurveyStatus status;
    private List<QuestionDto> questions; // 상세 보기 시 질문 목록
}
