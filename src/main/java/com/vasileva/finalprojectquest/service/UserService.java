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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserService {
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserAdminMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageHelper messageHelper;

    @Transactional(readOnly = true)
    public List<UserAdminDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Transactional
    public UserAdminDto createUser(UserSaveDto dto) {
        if (userRepository.existsByLogin(dto.getLogin())) {
            throw new RuntimeException("error.login.exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("error.email.exists");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        userStatsRepository.save(UserStats.builder()
                .user(savedUser)
                .total(0)
                .wins(0)
                .losses(0)
                .build());

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserAdminDto updateUser(Long id, UserSaveDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageHelper.getMessage("error.user.not_found", id)));

        existingUser.setLogin(dto.getLogin());
        existingUser.setEmail(dto.getEmail());
        existingUser.setRole(dto.getRole());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return userMapper.toDto(userRepository.save(existingUser));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    messageHelper.getMessage("error.user.not_found", id));
        }
        userStatsRepository.findByUserId(id).ifPresent(userStatsRepository::delete);
        userRepository.deleteById(id);
    }
}
