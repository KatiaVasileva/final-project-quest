package com.vasileva.finalprojectquest.dto;

import lombok.Data;

import java.util.List;

@Data
public class FullQuestDto {
    private Long id;
    private String title;
    private String description;
    private String text;
    private String image;
    private Long startQuestionId;
    private List<FullQuestionDto> questions;
    private Long creatorId;
}
