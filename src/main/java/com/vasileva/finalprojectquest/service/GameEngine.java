package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.AnswerRepository;
import com.vasileva.finalprojectquest.repository.QuestionRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameEngine {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserStatsService userStatsService;
    private final MessageHelper messageHelper;

    public boolean isFinalQuestion(Question question) {
        boolean isFinal = question.getAnswers() == null || question.getAnswers().isEmpty();
        log.debug("Checking question [ID: {}, Label: {}] if final: {}", question.getId(), question.getLabel(), isFinal);
        return isFinal;
    }

    public GameState startGame(User user, Quest quest) {
        log.debug("Initializing quest [ID: {}] for user[ID: {}]", quest.getId(), user.getId());
        Question startQuestion = questionRepository.findById(quest.getStartQuestionId())
                .orElseThrow(() -> {
                    log.error("Critical data error: quest [ID: {}] is missing a question with ID {}",
                            quest.getId(), quest.getStartQuestionId());
                    return new RuntimeException("error.start_question.not_found");
                });

        boolean isFinal = isFinalQuestion(startQuestion);

        return GameState.builder()
                .currentQuest(quest)
                .currentQuestion(startQuestion)
                .user(user)
                .isCompleted(isFinal)
                .build();
    }

    public GameState advanceGame(GameState currentState, Long answerId) {
        log.debug("Calculating state transition for GameState [ID: {}] by answer [ID: {}]", currentState.getId(), answerId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.answer.not_found", answerId)));

        String nextQuestionLabel = answer.getNextQuestionLabel();

        Question nextQuestion = questionRepository.findByLabelAndQuestId(nextQuestionLabel, currentState.getCurrentQuest().getId())
                .orElseThrow(() -> {
                    log.error("Quest failure [ID: {}]: question not found by label '{}'",
                            currentState.getCurrentQuest().getId(), nextQuestionLabel);
                    return new RuntimeException(messageHelper.getMessage(
                            "error.next_question.not_found", nextQuestionLabel));
                });

        boolean isFinal = isFinalQuestion(nextQuestion);

        if (isFinal) {
            log.info("User [ID: {}] has reached the final point of the quest. Sending a command to update statistics",
                    currentState.getUser().getId());
            UserStats stats = userStatsRepository.findByUserId(currentState.getUser().getId())
                    .orElseGet(() -> UserStats.builder().user(currentState.getUser()).build());

            userStatsService.updateUserStats(nextQuestion, stats);
        }

        return GameState.builder()
                .id(currentState.getId())
                .currentQuest(currentState.getCurrentQuest())
                .currentQuestion(nextQuestion)
                .user(currentState.getUser())
                .isCompleted(isFinal)
                .build();
    }
}
