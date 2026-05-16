// src/main/java/com/taskmanager/backend/exception/GlobalExceptionHandler.java

package com.taskmanager.backend.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
// ↑ "Fange Exceptions aus ALLEN Controllern ab und behandle sie zentral"
public class GlobalExceptionHandler {

    // ════════════════════════════════════════
    // VALIDIERUNGSFEHLER (@Valid schlägt fehl)
    // ════════════════════════════════════════
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationErrors(MethodArgumentNotValidException ex) {

        // Alle Feldvalidierungsfehler sammeln
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 400,
                "error", "Validierungsfehler",
                "fieldErrors", fieldErrors
        );
    /*
      Antwort:
      {
        "timestamp": "2024-03-15T10:30:00",
        "status": 400,
        "error": "Validierungsfehler",
        "fieldErrors": {
          "email": "Ungültige Email-Adresse",
          "password": "Mindestens 6 Zeichen"
        }
      }
    */
    }

    // ════════════════════════════════════════
    // NICHT GEFUNDEN
    // ════════════════════════════════════════
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(ResourceNotFoundException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 404,
                "error", ex.getMessage()
        );
    }

    // ════════════════════════════════════════
    // FALSCHES PASSWORT / USERNAME
    // ════════════════════════════════════════
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleBadCredentials(BadCredentialsException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 401,
                "error", "Ungültige Anmeldedaten"
        );
    }

    // ════════════════════════════════════════
    // ZUGRIFF VERWEIGERT
    // ════════════════════════════════════════
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDenied(AccessDeniedException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 403,
                "error", "Zugriff verweigert"
        );
    }

    // ════════════════════════════════════════
    // ALLE ANDEREN FEHLER
    // ════════════════════════════════════════
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGenericError(Exception ex) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", 500,
                "error", "Interner Serverfehler: " + ex.getMessage()
        );
    }
}