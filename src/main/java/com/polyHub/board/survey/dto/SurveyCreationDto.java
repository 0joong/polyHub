package com.polyHub.board.survey.dto;

import com.polyHub.board.survey.enums.QuestionType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SurveyCreationDto {
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private List<QuestionCreationDto> questions = new ArrayList<>();

    @Data
    public static class QuestionCreationDto {
        private String questionText;
        private QuestionType questionType;
        private boolean isRequired;
        private List<String> options = new ArrayList<>();
    }
}