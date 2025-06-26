package com.polyHub.board.survey.dto;

import com.polyHub.board.survey.enums.QuestionType;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SurveyResultDto {
    private Long id;
    private String title;
    private long totalParticipants;
    private List<QuestionResultDto> questions;

    @Data
    @Builder
    public static class QuestionResultDto {
        private String questionText;
        private QuestionType questionType;
        private Map<String, Long> optionCounts; // 객관식: {선택지: 갯수}
        private List<String> textAnswers;       // 주관식: 답변 목록
    }
}