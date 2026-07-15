package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.dto.UserAdminDto;
import com.vasileva.finalprojectquest.dto.UserSaveDto;
import com.vasileva.finalprojectquest.entity.User;
import com.vasileva.finalprojectquest.entity.UserStats;
import com.vasileva.finalprojectquest.mapper.UserAdminMapper;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserAdminMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageHelper messageHelper;

    @Transactional(readOnly = true)
    public List<UserAdminDto> getAllUsers() {
        log.debug("ADMIN request: get all users");
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Transactional
    public UserAdminDto createUser(UserSaveDto dto) {
        log.debug("ADMIN creating user. Checking uniqueness of username [{}] and email[{}]",
                dto.getLogin(), dto.getEmail());
        if (userRepository.existsByLogin(dto.getLogin())) {
            log.warn("ADMIN failure: username [{}] already exists", dto.getLogin());
            throw new RuntimeException("error.login.exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("ADMIN failure: email [{}] already exists", dto.getEmail());
            throw new RuntimeException("error.email.exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        log.debug("User [login: {}, role: {}] successfully saved by ADMIN", savedUser.getLogin(), savedUser.getRole());

        userStatsRepository.save(UserStats.builder()
                .user(savedUser)
                .total(0)
                .wins(0)
                .losses(0)
                .build());

        log.debug("User stats [ID: {}] successfully initialized", savedUser.getId());

        log.info("ADMIN successfully created user [{}] with ID: {} and role {}",
                savedUser.getLogin(), savedUser.getId(), savedUser.getRole());
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserAdminDto updateUser(Long id, UserSaveDto dto) {
        log.debug("ADMIN updating user with ID: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Admin data modification attempt failed: user with ID {} not found", id);
                    return new EntityNotFoundException(
                            messageHelper.getMessage("error.user.not_found", id));
                });

        existingUser.setLogin(dto.getLogin());
        existingUser.setEmail(dto.getEmail());
        existingUser.setRole(dto.getRole());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            log.debug("Administrator updated the password for user with ID: {}", id);
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("ADMIN successfully updeted user [{}]", updatedUser.getLogin());
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.debug("ADMIN deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("ADMIN deletion attempt failed: user with ID {} does not exist", id);
            throw new EntityNotFoundException(
                    messageHelper.getMessage("error.user.not_found", id));
        }
        userStatsRepository.findByUserId(id).ifPresent(stats -> {
            userStatsRepository.delete(stats);
            log.debug("Associated stats for user [ID: {}] have been successfully cleared", id);
        });

        userRepository.deleteById(id);
        log.info("User [ID: {}] has been deleted by ADMIN", id);
    }
}
