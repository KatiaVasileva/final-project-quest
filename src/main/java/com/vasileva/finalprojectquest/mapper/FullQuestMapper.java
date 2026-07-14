package com.vasileva.finalprojectquest.mapper;

import com.vasileva.finalprojectquest.dto.FullAnswerDto;
import com.vasileva.finalprojectquest.dto.FullQuestDto;
import com.vasileva.finalprojectquest.dto.FullQuestionDto;
import com.vasileva.finalprojectquest.entity.Answer;
import com.vasileva.finalprojectquest.entity.Quest;
import com.vasileva.finalprojectquest.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FullQuestMapper {
    @Mapping(source = "creator.id", target = "creatorId")
    FullQuestDto toDto(Quest quest);

    @Mapping(source = "creatorId", target = "creator.id")
    Quest toEntity(FullQuestDto questDto);

    List<FullQuestDto> toDtoList(List<Quest> quests);

    FullQuestionDto questionToDto(Question question);
    Question dtoToQuestion(FullQuestionDto dto);

    FullAnswerDto answerToDto(Answer answer);
    Answer dtoToAnswer(FullAnswerDto dto);
}
