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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestService {
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final FullQuestMapper fullQuestMapper;
    private final MessageHelper messageHelper;

    @Transactional(readOnly = true)
    public List<FullQuestDto> getAllQuestsWithDetails() {
        log.debug("Extracting a list of all quests from the database");
        List<Quest> quests = questRepository.findAll();
        return fullQuestMapper.toDtoList(quests);
    }

    @Transactional(readOnly = true)
    public FullQuestDto getQuestByIdWithDetails(Long id) {
        log.debug("Request to DB to get a quest with ID: {}", id);
        Quest quest = questRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Quest with ID {} not found", id);
                    return new EntityNotFoundException(
                            messageHelper.getMessage("error.quest.not_found", id));
                });
        return fullQuestMapper.toDto(quest);
    }

    @Transactional
    public FullQuestDto createQuest(FullQuestDto dto, String username) {
        log.debug("Start creating a new quest. Searching for user [{}]", username);
        User creator = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        Quest quest = fullQuestMapper.toEntity(dto);
        quest.setCreator(creator);

        if (quest.getQuestions() != null) {
            log.debug("Establishing bidirectional connections for {} quest questions", quest.getQuestions().size());
            quest.getQuestions().forEach(question -> {
                question.setQuest(quest);
                if (question.getAnswers() != null) {
                    question.getAnswers().forEach(answer -> answer.setQuestion(question));
                }
            });
        }

        Quest savedQuest = questRepository.save(quest);
        log.info("Quest '{}' successfully saved with ID: {}, author: [{}]",
                savedQuest.getTitle(), savedQuest.getId(), username);
        return fullQuestMapper.toDto(savedQuest);
    }

    @Transactional
    public FullQuestDto updateQuest(Long id, FullQuestDto dto) {
        log.debug("Start updating quest with ID: {}", id);
        Quest existingQuest = questRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.quest.not_found", id)));

        existingQuest.setTitle(dto.getTitle());
        existingQuest.setDescription(dto.getDescription());
        existingQuest.setText(dto.getText());
        existingQuest.setImage(dto.getImage());
        existingQuest.setStartQuestionId(dto.getStartQuestionId());

        log.debug("Clearing the old questions for quest ID: {}", id);
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
        log.info("Quest [ID: {}, title: '{}'] successfully updated", id, saved.getTitle());
        return fullQuestMapper.toDto(saved);
    }

    @Transactional
    public void deleteQuest(Long id) {
        log.debug("Checking existence of quest ID: {} before removal", id);
        if (!questRepository.existsById(id)) {
            log.warn("Attempting to delete a non-existent quest with ID: {}", id);
            throw new EntityNotFoundException(
                    messageHelper.getMessage("error.quest.not_found", id));
        }
        questRepository.deleteById(id);
        log.info("Quest [ID: {}] and all its cascaded questions/answers have been completely deleted", id);
    }

    @Transactional(readOnly = true)
    public List<FullQuestDto> getQuestsByCreator(String username) {
        log.debug("Requesting a list of author quests for creator: [{}]", username);
        User creator = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));
        List<Quest> quests = questRepository.findByCreatorId(creator.getId());
        return fullQuestMapper.toDtoList(quests);
    }
}
