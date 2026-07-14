package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.Game;
import com.vasileva.finalprojectquest.entity.GameState;
import com.vasileva.finalprojectquest.entity.Quest;
import com.vasileva.finalprojectquest.entity.User;
import com.vasileva.finalprojectquest.repository.GameRepository;
import com.vasileva.finalprojectquest.repository.GameStateRepository;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    private final GameStateRepository gameStateRepository;
    private final GameEngine gameEngine;

    @Transactional
    public Game startNewGame(Long questId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("Квест не найден"));

        Optional<Game> oldGameOpt = gameRepository.findByUserId(userId);
        if (oldGameOpt.isPresent()) {
            Game oldGame = oldGameOpt.get();
            GameState oldState = oldGame.getGameState();
            gameRepository.delete(oldGame);
            if (oldState != null) {
                gameStateRepository.delete(oldState);
            }
            gameRepository.flush();
            gameStateRepository.flush();
        }

        GameState initialState = gameEngine.startGame(user, quest);

        Game game = Game.builder()
                .quest(quest)
                .user(user)
                .currentQuestionId(initialState.getCurrentQuestion().getId())
                .gameState(initialState)
                .build();

        return gameRepository.save(game);
    }

    @Transactional
    public Game advanceGame(Long gameId, Long answerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Игра не найдена"));

        GameState nextState = gameEngine.advanceGame(game.getGameState(), answerId);

        game.setCurrentQuestionId(nextState.getCurrentQuestion().getId());
        game.setGameState(nextState);

        return gameRepository.save(game);
    }

    public Optional<Game> getActiveGameByUserId(Long userId) {
        return gameRepository.findByUserId(userId);
    }
}
