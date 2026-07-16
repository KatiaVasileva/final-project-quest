package com.vasileva.finalprojectquest.repository;

import com.vasileva.finalprojectquest.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByUserId(Long userId);

    Iterable<Game> findAllByQuestId(Long id);

}
