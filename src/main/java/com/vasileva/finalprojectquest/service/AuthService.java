package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.config.jwt.JwtTokenUtil;
import com.vasileva.finalprojectquest.dto.JwtResponse;
import com.vasileva.finalprojectquest.dto.LoginRequest;
import com.vasileva.finalprojectquest.dto.RefreshRequest;
import com.vasileva.finalprojectquest.dto.RegisterRequest;
import com.vasileva.finalprojectquest.entity.Role;
import com.vasileva.finalprojectquest.entity.User;
import com.vasileva.finalprojectquest.entity.UserStats;
import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.repository.UserStatsRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtResponse login(LoginRequest request) {
        log.debug("User authentication attempt via AuthenticationManager for login: [{}]", request.getLogin());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );

        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.debug("User [{}] has successfully authenticated. Detected roles: {}", username, roles);

        String accessToken = jwtTokenUtil.generateAccessToken(username, roles);
        String refreshToken = jwtTokenUtil.generateRefreshToken(username, roles);

        log.info("Successfully generated a new pair of JWT tokens for user [{}]", username);

        return new JwtResponse(accessToken, refreshToken);
    }

    @Transactional
    public String register(RegisterRequest request) {
        log.debug("Checking existence of username [{}] and email [{}] in the database",
                request.getLogin(), request.getEmail());

        if (userRepository.existsByLogin(request.getLogin())) {
            log.warn("Registration rejected: username [{}] is already taken", request.getLogin());
            throw new RuntimeException("error.login.exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected: email [{}] is already in use", request.getEmail());
            throw new RuntimeException("error.email.exists");
        }

        User user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        log.debug("User [{}] successfully saved in database", request.getLogin());

        UserStats stats = UserStats.builder()
                .user(user)
                .total(0)
                .wins(0)
                .losses(0)
                .build();

        userStatsRepository.save(stats);

        log.info("New user[{}] and user stats successfully registered", request.getLogin());
        return "User registered successfully";
    }

    public JwtResponse refreshTokens(RefreshRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        log.debug("Verification of provided refresh token");
        if (jwtTokenUtil.isTokenExpired(oldRefreshToken)) {
            log.warn("Token refresh failed: provided refresh token is expired");
            throw new RuntimeException("error.token.expired");
        }

        Claims claims = jwtTokenUtil.validateRefreshToken(oldRefreshToken);

        String username = claims.getSubject();
        List<String> roles = jwtTokenUtil.extractRoles(claims);
        log.debug("Refresh token is valid. Extracted subject [{}] and roles: {}", username, roles);

        String newAccessToken = jwtTokenUtil.generateAccessToken(username, roles);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(username, roles);

        log.info("Successfully performed token pair rotation for user [{}]", username);
        return new JwtResponse(newAccessToken, newRefreshToken);
    }
}
