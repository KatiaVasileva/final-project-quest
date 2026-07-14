package com.vasileva.finalprojectquest.dto;

import lombok.Data;

import java.util.List;

@Data
public class FullQuestionDto {
    private Long id;
    private String label;
    private String text;
    private List<FullAnswerDto> answers;
}
