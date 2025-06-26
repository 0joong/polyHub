package com.polyHub.board.survey.dto;

import com.polyHub.board.survey.enums.QuestionType;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuestionDto {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private int questionOrder;
    private boolean required;
    private List<OptionDto> options; // 객관식 질문의 선택지 목록
}