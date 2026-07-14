package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.entity.Question;
import com.vasileva.finalprojectquest.entity.UserStats;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatsService {
    private final UserStatsRepository userStatsRepository;

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
}
