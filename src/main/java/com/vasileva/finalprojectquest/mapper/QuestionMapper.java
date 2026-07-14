package com.vasileva.finalprojectquest.mapper;

import com.vasileva.finalprojectquest.dto.QuestionDto;
import com.vasileva.finalprojectquest.entity.Question;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionDto toDto(Question question);

    Question toEntity(QuestionDto questionDto);
}
