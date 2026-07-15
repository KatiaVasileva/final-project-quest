package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.dto.FullQuestDto;
import com.vasileva.finalprojectquest.entity.Quest;
import com.vasileva.finalprojectquest.entity.User;
import com.vasileva.finalprojectquest.mapper.FullQuestMapper;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final FullQuestMapper fullQuestMapper;
    private final MessageHelper messageHelper;

    @Transactional(readOnly = true)
    public List<FullQuestDto> getAllQuestsWithDetails() {
        List<Quest> quests = questRepository.findAll();
        return fullQuestMapper.toDtoList(quests);
    }

    @Transactional(readOnly = true)
    public FullQuestDto getQuestByIdWithDetails(Long id) {
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.quest.not_found", id)));
        return fullQuestMapper.toDto(quest);
    }

    @Transactional
    public FullQuestDto createQuest(FullQuestDto dto, String username) {
        User creator = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        Quest quest = fullQuestMapper.toEntity(dto);
        quest.setCreator(creator);

        if (quest.getQuestions() != null) {
            quest.getQuestions().forEach(question -> {
                question.setQuest(quest);
                if (question.getAnswers() != null) {
                    question.getAnswers().forEach(answer -> answer.setQuestion(question));
                }
            });
        }

        Quest savedQuest = questRepository.save(quest);
        return fullQuestMapper.toDto(savedQuest);
    }

    @Transactional
    public FullQuestDto updateQuest(Long id, FullQuestDto dto) {
        Quest existingQuest = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.quest.not_found", id)));

        existingQuest.setTitle(dto.getTitle());
        existingQuest.setDescription(dto.getDescription());
        existingQuest.setText(dto.getText());
        existingQuest.setImage(dto.getImage());
        existingQuest.setStartQuestionId(dto.getStartQuestionId());

        existingQuest.getQuestions().clear();

        Quest updatedFields = fullQuestMapper.toEntity(dto);
        if (updatedFields.getQuestions() != null) {
            updatedFields.getQuestions().forEach(question -> {
                question.setQuest(existingQuest);
                if (question.getAnswers() != null) {
                    question.getAnswers().forEach(answer -> answer.setQuestion(question));
                }
                existingQuest.getQuestions().add(question);
            });
        }

        Quest saved = questRepository.save(existingQuest);
        return fullQuestMapper.toDto(saved);
    }

    @Transactional
    public void deleteQuest(Long id) {
        if (!questRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    messageHelper.getMessage("error.quest.not_found", id));
        }
        questRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FullQuestDto> getQuestsByCreator(String username) {
        User creator = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));
        List<Quest> quests = questRepository.findByCreatorId(creator.getId());
        return fullQuestMapper.toDtoList(quests);
    }
}
