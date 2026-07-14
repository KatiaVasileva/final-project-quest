package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.QuestionDto;
import com.vasileva.finalprojectquest.entity.Game;
import com.vasileva.finalprojectquest.entity.User;
import com.vasileva.finalprojectquest.mapper.QuestionMapper;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;

    @PostMapping("/start")
    public ResponseEntity<QuestionDto> startGame(@RequestParam Long questId,
                                                 Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден в БД"));
        Game game = gameService.startNewGame(questId, user.getId());
        return ResponseEntity.ok(questionMapper.toDto(game.getGameState().getCurrentQuestion()));
    }

    @PostMapping("/play")
    public ResponseEntity<QuestionDto> playTurn(@RequestParam Long answerId,
                                                Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Текущий пользователь не найден в БД"));

        Game activeGame = gameService.getActiveGameByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("У вас нет активной игры"));

        Game updatedGame = gameService.advanceGame(activeGame.getId(), answerId);

        return ResponseEntity.ok(questionMapper.toDto(updatedGame.getGameState().getCurrentQuestion()));
    }
}
