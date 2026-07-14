package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.GameRepository;
import com.vasileva.finalprojectquest.repository.GameStateRepository;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    private final GameStateRepository gameStateRepository;
    private final GameEngine gameEngine;

    @Transactional
    public Question startNewGameByUsername(Long questId, String username) {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден в БД"));

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("Квест не найден"));

        gameRepository.findByUserId(user.getId()).ifPresent(oldGame -> {
            GameState oldState = oldGame.getGameState();
            gameRepository.delete(oldGame);
            if (oldState != null) {
                gameStateRepository.delete(oldState);
            }
            gameRepository.flush();
            gameStateRepository.flush();
        });

        GameState initialState = gameEngine.startGame(user, quest);

        Game game = Game.builder()
                .quest(quest)
                .user(user)
                .currentQuestionId(initialState.getCurrentQuestion().getId())
                .gameState(initialState)
                .build();

        gameRepository.save(game);

        return initialState.getCurrentQuestion();
    }

    @Transactional
    public Question advanceGameByUsername(String username, Long answerId) {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден в БД"));

        Game activeGame = gameRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("У вас нет активной игры"));

        GameState currentGameState = activeGame.getGameState();
        GameState nextStateCalculated = gameEngine.advanceGame(currentGameState, answerId);

        currentGameState.setCurrentQuestion(nextStateCalculated.getCurrentQuestion());
        currentGameState.setCompleted(nextStateCalculated.isCompleted());

        activeGame.setCurrentQuestionId(nextStateCalculated.getCurrentQuestion().getId());
        gameRepository.save(activeGame);

        return nextStateCalculated.getCurrentQuestion();
    }
}
