package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.QuestionDto;
import com.vasileva.finalprojectquest.entity.Question;
import com.vasileva.finalprojectquest.mapper.QuestionMapper;
import com.vasileva.finalprojectquest.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
@Slf4j
public class GameController {
    private final GameService gameService;
    private final QuestionMapper questionMapper;

    @PostMapping("/start")
    public ResponseEntity<QuestionDto> startGame(@RequestParam Long questId,
                                                 Authentication authentication) {
        log.info("REST request from user [{}] to start quest [{}]", authentication.getName(), questId);
        Question currentQuestion = gameService.startNewGameByUsername(questId, authentication.getName());

        log.info("Quest [ID: {}] successfully started for user [{}]. Starting question issued [ID: {}]",
                questId, authentication.getName(), currentQuestion.getId());
        return ResponseEntity.ok(questionMapper.toDto(currentQuestion));
    }

    @PostMapping("/play")
    public ResponseEntity<QuestionDto> playTurn(@RequestParam Long answerId,
                                                Authentication authentication) {
        log.info("REST request from user [{}]: answer option [ID: {}] selected", authentication.getName(), answerId);
        Question nextQuestion = gameService.advanceGameByUsername(authentication.getName(), answerId);

        log.info("User [{}] has been moved to the next question [ID: {}, Label: {}]",
                authentication.getName(), nextQuestion.getId(), nextQuestion.getLabel());
        return ResponseEntity.ok(questionMapper.toDto(nextQuestion));
    }
}
