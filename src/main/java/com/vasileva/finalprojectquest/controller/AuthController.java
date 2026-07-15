package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.*;
import com.vasileva.finalprojectquest.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register a new user: login [{}], email [{}]",
                request.getLogin(), request.getEmail());
        String message = authService.register(request);

        log.info("User [{}] successfully registered", request.getLogin());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to authenticate a user with login [{}]", request.getLogin());
        JwtResponse response = authService.login(request);

        log.info("User [{}] successfully logged in, a pair of JWT tokens has been generated",
                request.getLogin());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest request) {
        log.info("REST request to refresh a pair of JWT tokens");
        JwtResponse response = authService.refreshTokens(request);

        log.info("JWT tokens successfully refreshed");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> logout(Authentication authentication) {
        String username = authentication.getName();
        log.info("User [{}] has successfully logged out. The token has been deactivated on the client side.", username);
        return ResponseEntity.ok(
                new MessageResponse("You have successfully logged out. Please clear your browser's localStorage."));
    }
}
