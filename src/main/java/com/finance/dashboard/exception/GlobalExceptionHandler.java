package com.finance.dashboard.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Our own business exceptions ───────────────────────
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorResponse(ex.getMessage(), ex.getStatus().value()));
    }

    // ── @Valid failures ───────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field   = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            errors.put(field, message);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation failed",
                        HttpStatus.BAD_REQUEST.value(), errors));
    }

    // ── Constraint violations ─────────────────────────────
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()));
    }

    // ── Auth failures ─────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password", 401));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Access denied — insufficient role", 403));
    }

    // ── Catch-all ─────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Something went wrong: "
                        + ex.getMessage(), 500));
    }

    // ── Error response shape ──────────────────────────────
    public record ErrorResponse(
            String message,
            int status,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp) {

        public ErrorResponse(String message, int status) {
            this(message, status, null, LocalDateTime.now());
        }

        public ErrorResponse(String message, int status,
                             Map<String, String> fieldErrors) {
            this(message, status, fieldErrors, LocalDateTime.now());
        }
    }
}