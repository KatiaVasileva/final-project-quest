package com.vasileva.finalprojectquest.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private Long id;
    private String label;
    private String text;
    private List<AnswerDto> answers;
}
