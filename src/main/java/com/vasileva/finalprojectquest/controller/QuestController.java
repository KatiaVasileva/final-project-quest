package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.FullQuestDto;
import com.vasileva.finalprojectquest.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quests")
@RequiredArgsConstructor
public class QuestController {
    private final QuestService questService;

    @GetMapping
    public ResponseEntity<List<FullQuestDto>> getAllQuests() {
        return ResponseEntity.ok(questService.getAllQuestsWithDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FullQuestDto> getQuestById(@PathVariable Long id) {
        return ResponseEntity.ok(questService.getQuestByIdWithDetails(id));
    }

    @PostMapping
    public ResponseEntity<FullQuestDto> createQuest(@RequestBody FullQuestDto dto, Authentication authentication) {
        FullQuestDto createdQuest = questService.createQuest(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FullQuestDto> updateQuest(@PathVariable Long id, @RequestBody FullQuestDto dto) {
        return ResponseEntity.ok(questService.updateQuest(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuest(@PathVariable Long id) {
        questService.deleteQuest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<FullQuestDto>> getMyQuests(Authentication authentication) {
        return ResponseEntity.ok(questService.getQuestsByCreator(authentication.getName()));
    }
}
