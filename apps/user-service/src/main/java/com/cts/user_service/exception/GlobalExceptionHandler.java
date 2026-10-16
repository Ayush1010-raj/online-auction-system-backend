// ─────────────────────────────────────────
// exception/GlobalExceptionHandler.java
// ─────────────────────────────────────────
package com.cts.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
// @RestControllerAdvice intercepts ALL exceptions thrown from ANY controller
public class GlobalExceptionHandler {

    // 1. Bean Validation failures (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                fieldErrors
        ));
    }

    // 2. Business logic errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null
        ));
    }

    // 3. User not found
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(
            jakarta.persistence.EntityNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null
        ));
    }

    // 4. Any unexpected error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again.",
                null
        ));
    }

    private Map<String, Object> buildError(HttpStatus status, String message, Object details) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        if (details != null) {
            error.put("details", details);  // field-level errors from @Valid
        }
        return error;
    }
}