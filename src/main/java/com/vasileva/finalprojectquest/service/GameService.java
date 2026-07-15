package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.GameRepository;
import com.vasileva.finalprojectquest.repository.GameStateRepository;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
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
    private final MessageHelper messageHelper;
    @Transactional
    public Question startNewGameByUsername(Long questId, String username) {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.quest.not_found", questId)));

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
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        Game activeGame = gameRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("error.game.not_found"));

        GameState currentGameState = activeGame.getGameState();
        GameState nextStateCalculated = gameEngine.advanceGame(currentGameState, answerId);

        currentGameState.setCurrentQuestion(nextStateCalculated.getCurrentQuestion());
        currentGameState.setCompleted(nextStateCalculated.isCompleted());

        activeGame.setCurrentQuestionId(nextStateCalculated.getCurrentQuestion().getId());
        gameRepository.save(activeGame);

        return nextStateCalculated.getCurrentQuestion();
    }
}
