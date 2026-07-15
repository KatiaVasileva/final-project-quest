package com.vasileva.finalprojectquest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Long id;
    private String userLogin;
    private int totalGames;
    private int wins;
    private int losses;
    private double winRate;
    private long globalRank;
}
