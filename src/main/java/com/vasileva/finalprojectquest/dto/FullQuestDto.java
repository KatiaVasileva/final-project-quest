package com.vasileva.finalprojectquest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class FullQuestDto {
    private Long id;

    @NotBlank(message = "validation.quest.title.empty")
    @Size(max = 255, message = "validation.quest.title.size")
    private String title;

    @NotBlank(message = "validation.quest.description.empty")
    private String description;

    @NotBlank(message = "validation.quest.text.empty")
    private String text;

    private String image;

    private Long startQuestionId;

    private List<FullQuestionDto> questions;

    private Long creatorId;
}
