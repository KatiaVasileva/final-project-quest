package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.UserStatsDto;
import com.vasileva.finalprojectquest.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Slf4j
public class UserStatsController {
    private final UserStatsService userStatsService;

    @GetMapping
    public ResponseEntity<UserStatsDto> getMyStats(Authentication authentication) {
        log.info("REST request to get user's [{}] stats", authentication.getName());
        UserStatsDto statsDto = userStatsService.getStatsByUsername(authentication.getName());
        return ResponseEntity.ok(statsDto);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserStatsDto>> getLeaderboard() {
        log.info("REST request to get leaderboard");
        List<UserStatsDto> leaderboard = userStatsService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}

