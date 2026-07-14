package com.vasileva.finalprojectquest.dto;

import lombok.Data;

@Data
public class AnswerDto {
    private Long id;
    private String text;
    private String description;
    private String nextQuestionLabel;
}
