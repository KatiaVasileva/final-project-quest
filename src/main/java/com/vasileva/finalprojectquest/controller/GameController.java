package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.QuestionDto;
import com.vasileva.finalprojectquest.entity.Question;
import com.vasileva.finalprojectquest.mapper.QuestionMapper;
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
    private final QuestionMapper questionMapper;

    @PostMapping("/start")
    public ResponseEntity<QuestionDto> startGame(@RequestParam Long questId,
                                                 Authentication authentication) {
        Question currentQuestion = gameService.startNewGameByUsername(questId, authentication.getName());
        return ResponseEntity.ok(questionMapper.toDto(currentQuestion));
    }

    @PostMapping("/play")
    public ResponseEntity<QuestionDto> playTurn(@RequestParam Long answerId,
                                                Authentication authentication) {
        Question nextQuestion = gameService.advanceGameByUsername(authentication.getName(), answerId);
        return ResponseEntity.ok(questionMapper.toDto(nextQuestion));
    }
}
