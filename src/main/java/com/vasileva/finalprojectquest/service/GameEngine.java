package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.AnswerRepository;
import com.vasileva.finalprojectquest.repository.QuestionRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameEngine {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserStatsService userStatsService;
    private final MessageHelper messageHelper;

    public boolean isFinalQuestion(Question question) {
        return question.getAnswers() == null || question.getAnswers().isEmpty();
    }

    public GameState startGame(User user, Quest quest) {
        Question startQuestion = questionRepository.findById(quest.getStartQuestionId())
                .orElseThrow(() -> new RuntimeException("error.start_question.not_found"));

        boolean isFinal = isFinalQuestion(startQuestion);

        return GameState.builder()
                .currentQuest(quest)
                .currentQuestion(startQuestion)
                .user(user)
                .isCompleted(isFinal)
                .build();
    }

    public GameState advanceGame(GameState currentState, Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.answer.not_found", answerId)));

        String nextQuestionLabel = answer.getNextQuestionLabel();

        Question nextQuestion = questionRepository.findByLabelAndQuestId(nextQuestionLabel, currentState.getCurrentQuest().getId())
                .orElseThrow(() -> new RuntimeException(
                        messageHelper.getMessage("error.next_question.not_found", nextQuestionLabel)));

        boolean isFinal = isFinalQuestion(nextQuestion);

        if (isFinal) {
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
