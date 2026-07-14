package com.vasileva.finalprojectquest.repository;

import com.vasileva.finalprojectquest.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
}
