package com.vasileva.finalprojectquest.util;

import com.vasileva.finalprojectquest.dto.LoginRequest;
import com.vasileva.finalprojectquest.dto.RegisterRequest;
import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.QuestionRepository;

import java.util.Collections;
import java.util.List;

public class TestDataFactory {

    public static User createTestUser() {
        return User.builder()
                .id(1L)
                .login("player")
                .email("player@quest.com")
                .role(Role.USER)
                .build();
    }

    public static Quest createTestQuest() {
        return Quest.builder()
                .id(10L)
                .title("Test Quest")
                .description("Description")
                .startQuestionId(100L)
                .build();
    }

    public static Question createIntermediateQuestion() {
        Question question = Question.builder()
                .id(100L)
                .label("1")
                .text("Text")
                .build();
        question.setAnswers(List.of(Answer.builder().id(5L).text("Advance").build()));
        return question;
    }

    public static Question createFinalQuestion(String label, String text) {
        return Question.builder()
                .id(101L)
                .label(label)
                .text(text)
                .answers(Collections.emptyList())
                .build();
    }

    public static Answer createTestAnswer(Question sourceQuestion, String nextLabel) {
        return Answer.builder()
                .id(200L)
                .question(sourceQuestion)
                .text("Make choice")
                .nextQuestionLabel(nextLabel)
                .build();
    }

    public static GameState createInitialGameState(User user, Quest quest, Question question) {
        return GameState.builder()
                .id(555L)
                .currentQuest(quest)
                .currentQuestion(question)
                .user(user)
                .isCompleted(false)
                .build();
    }

    public static RegisterRequest createRegisterRequest(String login, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setLogin(login);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    public static LoginRequest createLoginRequest(String login, String password) {
        LoginRequest request = new LoginRequest();
        request.setLogin(login);
        request.setPassword(password);
        return request;
    }

    public static Quest createAndSaveTestQuest(QuestRepository questRepository,
            QuestionRepository questionRepository) {

        Quest testQuest = questRepository.save(Quest.builder()
                .title("Test Quest")
                .description("Description")
                .text("Text")
                .startQuestionId(null)
                .build());

        Question startQuestion = Question.builder()
                .label("1")
                .text("Start?")
                .quest(testQuest)
                .build();

        startQuestion = questionRepository.save(startQuestion);
        testQuest.setStartQuestionId(startQuestion.getId());

        return questRepository.save(testQuest);
    }
}
