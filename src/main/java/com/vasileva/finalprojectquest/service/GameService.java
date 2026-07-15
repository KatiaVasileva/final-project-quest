package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.*;
import com.vasileva.finalprojectquest.repository.GameRepository;
import com.vasileva.finalprojectquest.repository.GameStateRepository;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    private final GameStateRepository gameStateRepository;
    private final GameEngine gameEngine;
    private final MessageHelper messageHelper;
    @Transactional
    public Question startNewGameByUsername(Long questId, String username) {
        log.debug("Start new game for user [{}]", username);

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.quest.not_found", questId)));

        gameRepository.findByUserId(user.getId()).ifPresent(oldGame -> {
            log.info("Active game for user [ID: {}] found. Deleting old session before restart", user.getId());
            GameState oldState = oldGame.getGameState();
            gameRepository.delete(oldGame);
            if (oldState != null) {
                gameStateRepository.delete(oldState);
            }
            gameRepository.flush();
            gameStateRepository.flush();
            log.debug("Old session for user [ID: {}] successfully deleted", user.getId());
        });

        GameState initialState = gameEngine.startGame(user, quest);

        Game game = Game.builder()
                .quest(quest)
                .user(user)
                .currentQuestionId(initialState.getCurrentQuestion().getId())
                .gameState(initialState)
                .build();

        gameRepository.save(game);

        log.info("User [{}] successfully started quest '{}'. Game [ID: {}] created",
                username, quest.getTitle(), game.getId());
        return initialState.getCurrentQuestion();
    }

    @Transactional
    public Question advanceGameByUsername(String username, Long answerId) {
        log.debug("User [{}] makes a move, processing answer option [ID: {}]", username, answerId);

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        Game activeGame = gameRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Move rejected: user [{}] has no active game in the database", username);
                    return new RuntimeException("error.game.not_found");
                });

        GameState currentGameState = activeGame.getGameState();
        GameState nextStateCalculated = gameEngine.advanceGame(currentGameState, answerId);

        currentGameState.setCurrentQuestion(nextStateCalculated.getCurrentQuestion());
        currentGameState.setCompleted(nextStateCalculated.isCompleted());

        activeGame.setCurrentQuestionId(nextStateCalculated.getCurrentQuestion().getId());
        gameRepository.save(activeGame);

        log.info("Game [ID: {}] updated for user [{}]. Current completion status: {}",
                activeGame.getId(), username, nextStateCalculated.isCompleted());
        return nextStateCalculated.getCurrentQuestion();
    }
}
