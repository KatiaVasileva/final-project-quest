package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.AnswerRepository;
import com.vasileva.finalprojectquest.repository.QuestionRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import com.vasileva.finalprojectquest.util.TestDataFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit testing for GameEngine")
public class GameEngineTest {
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private UserStatsRepository userStatsRepository;
    @Mock
    private UserStatsService userStatsService;
    @Mock
    private MessageHelper messageHelper;

    @InjectMocks
    private GameEngine gameEngine;

    @Test
    @DisplayName("isFinalQuestion: should return true when answer list is null")
    void isFinalQuestion_ShouldReturnTrue_WhenAnswersListIsNull() {
        Question question = Question.builder().answers(null).build();
        assertThat(gameEngine.isFinalQuestion(question)).isTrue();
    }

    @Test
    @DisplayName("isFinalQuestion: should return true when answer list is empty")
    void isFinalQuestion_ShouldReturnTrue_WhenAnswersListIsEmpty() {
        Question question = Question.builder().answers(Collections.emptyList()).build();
        assertThat(gameEngine.isFinalQuestion(question)).isTrue();
    }

    @Test
    @DisplayName("isFinalQuestion: should return false when question has answers")
    void isFinalQuestion_ShouldReturnFalse_WhenQuestionHasAnswers() {
        Question question = TestDataFactory.createIntermediateQuestion();
        assertThat(gameEngine.isFinalQuestion(question)).isFalse();
    }

    @Test
    @DisplayName("startGame: successful start with valid data")
    void startGame_ShouldReturnCorrectInitialState_WhenDataIsValid() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();
        Question startQuestion = TestDataFactory.createIntermediateQuestion();

        when(questionRepository.findById(quest.getStartQuestionId())).thenReturn(Optional.of(startQuestion));

        GameState resultState = gameEngine.startGame(user, quest);

        assertThat(resultState).isNotNull();
        assertThat(resultState.getCurrentQuest()).isEqualTo(quest);
        assertThat(resultState.getCurrentQuestion()).isEqualTo(startQuestion);
        assertThat(resultState.getUser()).isEqualTo(user);
        assertThat(resultState.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("startGame: should throw exception when start question not found")
    void startGame_ShouldThrowException_WhenStartQuestionNotFound() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();

        when(questionRepository.findById(quest.getStartQuestionId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameEngine.startGame(user, quest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error.start_question.not_found");
    }

    @Test
    @DisplayName("advanceGame: should transit to next intermediate question when answer is normal")
    void advanceGame_ShouldTransitionToNextIntermediateQuestion_WhenAnswerIsNormal() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();
        Question currentQuestion = TestDataFactory.createIntermediateQuestion();
        GameState currentState = TestDataFactory.createInitialGameState(user, quest, currentQuestion);

        Answer chosenAnswer = TestDataFactory.createTestAnswer(currentQuestion, "2");
        Question nextQuestion = TestDataFactory.createIntermediateQuestion();
        nextQuestion.setLabel("2");

        when(answerRepository.findById(200L)).thenReturn(Optional.of(chosenAnswer));
        when(questionRepository.findByLabelAndQuestId("2", quest.getId())).thenReturn(Optional.of(nextQuestion));

        GameState nextState = gameEngine.advanceGame(currentState, 200L);

        assertThat(nextState).isNotNull();
        assertThat(nextState.getId()).isEqualTo(currentState.getId());
        assertThat(nextState.getCurrentQuestion()).isEqualTo(nextQuestion);
        assertThat(nextState.isCompleted()).isFalse();

        verifyNoInteractions(userStatsRepository, userStatsService);
    }

    @Test
    @DisplayName("advanceGame: should transit to final question and update stats")
    void advanceGame_ShouldTransitionToFinalQuestion_AndUpdateExistingStats() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();
        Question currentQuestion = TestDataFactory.createIntermediateQuestion();
        GameState currentState = TestDataFactory.createInitialGameState(user, quest, currentQuestion);

        Answer chosenAnswer = TestDataFactory.createTestAnswer(currentQuestion, "+810");
        Question finalQuestion = TestDataFactory.createFinalQuestion("+810", "Win!");
        UserStats existingStats = UserStats.builder().id(7L).user(user).wins(5).build();

        when(answerRepository.findById(200L)).thenReturn(Optional.of(chosenAnswer));
        when(questionRepository.findByLabelAndQuestId("+810", quest.getId())).thenReturn(Optional.of(finalQuestion));
        when(userStatsRepository.findByUserId(user.getId())).thenReturn(Optional.of(existingStats));

        GameState nextState = gameEngine.advanceGame(currentState, 200L);

        assertThat(nextState.isCompleted()).isTrue();
        verify(userStatsService, times(1)).updateUserStats(finalQuestion, existingStats);
    }

    @Test
    @DisplayName("advanceGame: should transit to final question and create new stats if missing")
    void advanceGame_ShouldTransitionToFinalQuestion_AndCreateNewStatsIfMissing() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();
        Question currentQuestion = TestDataFactory.createIntermediateQuestion();
        GameState currentState = TestDataFactory.createInitialGameState(user, quest, currentQuestion);

        Answer chosenAnswer = TestDataFactory.createTestAnswer(currentQuestion, "-910");
        Question finalQuestion = TestDataFactory.createFinalQuestion("-910", "Lose...");

        when(answerRepository.findById(200L)).thenReturn(Optional.of(chosenAnswer));
        when(questionRepository.findByLabelAndQuestId("-910", quest.getId())).thenReturn(Optional.of(finalQuestion));
        when(userStatsRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        GameState nextState = gameEngine.advanceGame(currentState, 200L);

        assertThat(nextState.isCompleted()).isTrue();
        verify(userStatsService, times(1)).updateUserStats(eq(finalQuestion), any(UserStats.class));
    }

    @Test
    @DisplayName("advanceGame: should throw exception when answer not found")
    void advanceGame_ShouldThrowException_WhenAnswerNotFound() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();
        GameState currentState = TestDataFactory.createInitialGameState(user, quest, TestDataFactory.createIntermediateQuestion());

        when(answerRepository.findById(200L)).thenReturn(Optional.empty());
        when(messageHelper.getMessage("error.answer.not_found", 200L)).thenReturn("Answer not found");

        assertThatThrownBy(() -> gameEngine.advanceGame(currentState, 200L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Answer not found");
    }

    @Test
    @DisplayName("advanceGame: throw exception when next question not found")
    void advanceGame_ShouldThrowException_WhenNextQuestionNotFoundByLabel() {
        User user = TestDataFactory.createTestUser();
        Quest quest = TestDataFactory.createTestQuest();
        Question currentQuestion = TestDataFactory.createIntermediateQuestion();
        GameState currentState = TestDataFactory.createInitialGameState(user, quest, currentQuestion);

        Answer chosenAnswer = TestDataFactory.createTestAnswer(currentQuestion, "999");

        when(answerRepository.findById(200L)).thenReturn(Optional.of(chosenAnswer));
        when(questionRepository.findByLabelAndQuestId("999", quest.getId())).thenReturn(Optional.empty());
        when(messageHelper.getMessage("error.next_question.not_found", "999")).thenReturn("Question not found");

        assertThatThrownBy(() -> gameEngine.advanceGame(currentState, 200L))
                .isInstanceOf(RuntimeException.class).hasMessage("Question not found");
    }

}

