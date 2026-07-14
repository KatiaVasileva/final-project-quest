package com.vasileva.finalprojectquest.mapper;

import com.vasileva.finalprojectquest.dto.QuestDto;
import com.vasileva.finalprojectquest.dto.QuestionDto;
import com.vasileva.finalprojectquest.entity.Quest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestMapper {

    QuestionDto toDto(Quest quest);

    Quest toEntity(QuestionDto questDto);

    List<QuestDto> toDtoList(List<Quest> quests);
}
