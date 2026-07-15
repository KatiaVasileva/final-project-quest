package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.dto.UserStatsDto;
import com.vasileva.finalprojectquest.entity.Question;
import com.vasileva.finalprojectquest.entity.User;
import com.vasileva.finalprojectquest.entity.UserStats;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatsService {
    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository;
    private final MessageHelper messageHelper;

    @Transactional
    public void updateUserStats(Question finalQuestion, UserStats stats) {
        log.info("Processing statistics update for user [ID: {}]. Final question reached [ID: {}, Label: {}]",
                stats.getUser().getId(), finalQuestion.getId(), finalQuestion.getLabel());

        int oldTotal = stats.getTotal();
        stats.setTotal(oldTotal + 1);

        if (finalQuestion.getLabel() != null && finalQuestion.getLabel().startsWith("+")) {
            stats.setWins(stats.getWins() + 1);
            log.debug("Incremented wins counter for user [ID: {}]. Total wins: {}",
                    stats.getUser().getId(), stats.getWins());
        } else if (finalQuestion.getLabel() != null && finalQuestion.getLabel().startsWith("-")) {
            stats.setLosses(stats.getLosses() + 1);
            log.debug("Incremented losses counter for user [ID: {}]. Total losses: {}",
                    stats.getUser().getId(), stats.getLosses());
        }

        userStatsRepository.save(stats);
        log.info("Successfully updated statistics for user [ID: {}]. Total games: {}",
                stats.getUser().getId(), stats.getTotal());

    }

    @Transactional(readOnly = true)
    public UserStatsDto getStatsByUsername(String username) {
        log.info("Requesting statistics for username: {}", username);
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> {
                    log.error("Authentication/Fetch failed: user with username [{}] not found in the database", username);
                    return new EntityNotFoundException(
                            messageHelper.getMessage("error.username.not_found", username));
                });

        UserStats stats = userStatsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.warn("Statistics profile not found for user [ID: {}]. Initializing new default profile", user.getId());
                    return userStatsRepository.save(UserStats.builder()
                            .user(user)
                            .total(0)
                            .wins(0)
                            .losses(0)
                            .build());
                });

        log.debug("Calculating global rank for user [ID: {}] based on {} wins", user.getId(), stats.getWins());
        long rank = userStatsRepository.calculateGlobalRank(stats.getWins());

        return convertToDto(stats, rank);
    }

    @Transactional(readOnly = true)
    public List<UserStatsDto> getLeaderboard() {
        log.info("Fetching global leaderboard top 10 players from the database");

        List<UserStats> topStats = userStatsRepository.findTop10ByOrderByWinsDesc();
        log.debug("Found {} top entries for the leaderboard", topStats.size());

        return topStats.stream()
                .map(stats -> {
                    long rank = userStatsRepository.calculateGlobalRank(stats.getWins());
                    return convertToDto(stats, rank);
                })
                .collect(Collectors.toList());
    }

    private UserStatsDto convertToDto(UserStats stats, long rank) {
        double winRate = 0.0;
        if (stats.getTotal() > 0) {
            winRate = Math.round(((double) stats.getWins() / stats.getTotal()) * 100.0 * 10.0) / 10.0;
        }

        log.trace("Converting UserStats entity to DTO for user [{}]. Calculated WinRate: {}%, Rank: {}",
                stats.getUser().getLogin(), winRate, rank);

        return UserStatsDto.builder()
                .id(stats.getId())
                .userLogin(stats.getUser().getLogin())
                .totalGames(stats.getTotal())
                .wins(stats.getWins())
                .losses(stats.getLosses())
                .winRate(winRate)
                .globalRank(rank)
                .build();
    }
}
