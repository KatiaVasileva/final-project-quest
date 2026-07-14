package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.AnswerRepository;
import com.vasileva.finalprojectquest.repository.QuestionRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameEngine {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserStatsService userStatsService;

    public boolean isFinalQuestion(Question question) {
        return question.getAnswers() == null || question.getAnswers().isEmpty();
    }

    public GameState startGame(User user, Quest quest) {
        Question startQuestion = questionRepository.findById(quest.getStartQuestionId())
                .orElseThrow(() -> new RuntimeException("Стартовый вопрос не найден"));

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
                .orElseThrow(() -> new RuntimeException("Вариант ответа не найден"));

        String nextQuestionLabel = answer.getNextQuestionLabel();

        Question nextQuestion = questionRepository.findByLabelAndQuestId(nextQuestionLabel, currentState.getCurrentQuest().getId())
                .orElseThrow(() -> new RuntimeException("Следующий вопрос не найден по лейблу: " + nextQuestionLabel));

        boolean isFinal = isFinalQuestion(nextQuestion);

        if (isFinal) {
            UserStats stats = userStatsRepository.findByUserId(currentState.getUser().getId())
                    .orElseGet(() -> UserStats.builder().user(currentState.getUser()).build()); // Создаем пустую, если не нашли

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
