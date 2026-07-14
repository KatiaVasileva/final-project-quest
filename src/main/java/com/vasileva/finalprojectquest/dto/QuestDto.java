package com.vasileva.finalprojectquest.dto;

import lombok.Data;

@Data
public class QuestDto {
    private Long id;
    private String title;
    private String description;
    private String text;
    private String image;
    private Long startQuestionId;
}
