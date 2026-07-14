package com.vasileva.finalprojectquest.mapper;

import com.vasileva.finalprojectquest.dto.AnswerDto;
import com.vasileva.finalprojectquest.entity.Answer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    AnswerDto toDto(Answer answer);

    Answer toEntity(AnswerDto answerDto);
}
