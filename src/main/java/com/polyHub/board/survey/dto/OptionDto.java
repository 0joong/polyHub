package com.polyHub.board.survey.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionDto {
    private Long id;
    private String optionText;
    private int optionOrder;
}