package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.FullQuestDto;
import com.vasileva.finalprojectquest.service.QuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
@Slf4j
public class QuestController {
    private final QuestService questService;

    @GetMapping
    public ResponseEntity<List<FullQuestDto>> getAllQuests() {
        log.info("REST request to get all quests");
        List<FullQuestDto> quests = questService.getAllQuestsWithDetails();
        log.info("Successfully received quests: {}", quests.size());
        return ResponseEntity.ok(quests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FullQuestDto> getQuestById(@PathVariable Long id) {
        log.info("REST request to get a quest [ID: {}]", id);
        return ResponseEntity.ok(questService.getQuestByIdWithDetails(id));
    }

    @PostMapping
    public ResponseEntity<FullQuestDto> createQuest(@Valid @RequestBody FullQuestDto dto, Authentication authentication) {
        log.info("REST request from user [{}] to create a new quest titled '{}'",
                authentication.getName(), dto.getTitle());
        FullQuestDto createdQuest = questService.createQuest(dto, authentication.getName());

        log.info("User [{}] successfully created quest '{}' with [ID: {}]",
                authentication.getName(), createdQuest.getTitle(), createdQuest.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FullQuestDto> updateQuest(@PathVariable Long id, @Valid @RequestBody FullQuestDto dto) {
        log.info("REST request to update quest [ID: {}]", id);
        FullQuestDto updatedQuest = questService.updateQuest(id, dto);

        log.info("Quest [ID: {}] successfully updated", id);
        return ResponseEntity.ok(updatedQuest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuest(@PathVariable Long id) {
        log.warn("REST request to delete quest [ID: {}]", id);
        questService.deleteQuest(id);

        log.info("Quest [ID: {}] successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<FullQuestDto>> getMyQuests(Authentication authentication) {
        log.info("REST request from user [{}] to get a list of the user's quests", authentication.getName());
        return ResponseEntity.ok(questService.getQuestsByCreator(authentication.getName()));
    }
}
