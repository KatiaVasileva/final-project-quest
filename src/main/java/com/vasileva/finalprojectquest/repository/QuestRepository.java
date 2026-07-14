package com.vasileva.finalprojectquest.repository;

import com.vasileva.finalprojectquest.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByCreatorId(Long creatorId);
}
