package com.vasileva.finalprojectquest.controller;

import com.vasileva.finalprojectquest.dto.JwtResponse;
import com.vasileva.finalprojectquest.dto.LoginRequest;
import com.vasileva.finalprojectquest.dto.RefreshRequest;
import com.vasileva.finalprojectquest.dto.RegisterRequest;
import com.vasileva.finalprojectquest.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            String message = authService.register(request);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            JwtResponse response = authService.refreshTokens(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            log.info("Пользователь [{}] успешно вышел из системы. Токен деактивирован на стороне клиента.", username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Вы успешно вышли из системы. Очистите localStorage в браузере."
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Запрос отклонен: пользователь не был аутентифицирован."
        ));
    }
}
