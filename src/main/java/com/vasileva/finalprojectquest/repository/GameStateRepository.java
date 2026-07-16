package com.vasileva.finalprojectquest.repository;

import com.vasileva.finalprojectquest.entity.GameState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameStateRepository extends JpaRepository<GameState, Long> {

    Optional<GameState> findByUserId(Long userId);

    Iterable<GameState> findAllByCurrentQuestId(Long id);
}
