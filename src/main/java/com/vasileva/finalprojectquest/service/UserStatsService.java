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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatsService {
    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository;
    private final MessageHelper messageHelper;

    @Transactional
    public void updateUserStats(Question finalQuestion, UserStats stats) {
        stats.setTotal(stats.getTotal() + 1);

        if (finalQuestion.getLabel() != null && finalQuestion.getLabel().startsWith("+")) {
            stats.setWins(stats.getWins() + 1);
        } else if (finalQuestion.getLabel() != null && finalQuestion.getLabel().startsWith("-")) {
            stats.setLosses(stats.getLosses() + 1);
        }

        userStatsRepository.save(stats);
    }

    @Transactional(readOnly = true)
    public UserStatsDto getStatsByUsername(String username) {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.username.not_found", username)));

        UserStats stats = userStatsRepository.findByUserId(user.getId())
                .orElseGet(() -> userStatsRepository.save(UserStats.builder()
                        .user(user)
                        .total(0)
                        .wins(0)
                        .losses(0)
                        .build()));

        long rank = userStatsRepository.calculateGlobalRank(stats.getWins());

        return convertToDto(stats, rank);
    }

    @Transactional(readOnly = true)
    public List<UserStatsDto> getLeaderboard() {
        return userStatsRepository.findTop10ByOrderByWinsDesc().stream()
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
