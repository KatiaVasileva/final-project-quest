package com.vasileva.finalprojectquest.exception;

import com.vasileva.finalprojectquest.util.MessageHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    private final MessageHelper messageHelper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("DTO incoming data validation error");

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .map(code -> {
                    try {
                        return messageHelper.getMessage(code);
                    } catch (Exception ex) {
                        return code;
                    }
                })
                .collect(Collectors.joining(", "));

        ErrorResponse response = new ErrorResponse("VALIDATION_FAILED", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse>  handleFoodNotFoundException(EntityNotFoundException e) {
        log.error("Not found: {}", e.getMessage());
        String message = e.getMessage().startsWith("error.") ? messageHelper.getMessage(e.getMessage()) : e.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("NOT FOUND", message));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Application business error: {}", ex.getMessage());
        String message = ex.getMessage().startsWith("error.") ? messageHelper.getMessage(ex.getMessage()) : ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("BUSINESS_ERROR", message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Failed login attempt: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse("UNAUTHORIZED", messageHelper.getMessage("error.unauthorized"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse("FORBIDDEN", messageHelper.getMessage("error.forbidden"));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected system error 500: ", ex);
        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR",
                messageHelper.getMessage("error.internal_server_error"));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
