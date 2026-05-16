// src/main/java/com/taskmanager/backend/controller/AuthController.java

package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.*;
import com.taskmanager.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController        // ← @Controller + @ResponseBody (gibt JSON zurück)
@RequestMapping("/api/auth")
// ↑ Alle Endpoints in dieser Klasse beginnen mit /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
            // @RequestBody = "Lese den JSON-Body und mappe ihn auf RegisterRequest"
            // @Valid = "Führe die Validierungsregeln aus (@NotBlank, @Email, etc.)"
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // ↑ HTTP 201 Created + JSON Body
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
        // ↑ HTTP 200 OK + JSON Body
    }
}