package com.vasileva.finalprojectquest.repository;

import com.vasileva.finalprojectquest.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    Optional<UserStats> findByUserId(Long userId);

    List<UserStats> findTop10ByOrderByWinsDesc();

    @Query("SELECT COUNT(us) + 1 FROM UserStats us WHERE us.wins > :userWins")
    long calculateGlobalRank(@Param("userWins") int userWins);
}
