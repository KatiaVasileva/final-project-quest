package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.UserAdminDto;
import com.vasileva.finalprojectquest.dto.UserSaveDto;
import com.vasileva.finalprojectquest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserAdminDto>> getAll() {
        log.info("REST request from ADMIN to get all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserAdminDto> create(@Valid @RequestBody UserSaveDto dto) {
        log.info("REST request from ADMIN to create a new user: login [{}], role [{}]",
                dto.getLogin(), dto.getRole());
        UserAdminDto createdUser = userService.createUser(dto);

        log.info("ADMIN successfully created user [{}] with [ID: {}]",
                createdUser.getLogin(), createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAdminDto> update(@PathVariable Long id, @Valid @RequestBody UserSaveDto dto) {
        log.info("REST request from ADMIN to update user [ID: {}]", id);
        UserAdminDto updatedUser = userService.updateUser(id, dto);

        log.info("User [{}] (ID: {}) successfully updated by ADMIN", updatedUser.getLogin(), id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("REST request from ADMIN to delete user [ID: {}]", id);
        userService.deleteUser(id);

        log.info("User [ID: {}] successfully deleted with stats", id);
        return ResponseEntity.noContent().build();
    }
}
