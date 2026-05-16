// src/main/java/com/taskmanager/backend/service/AuthService.java

package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.*;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ════════════════════════════════════════
    // REGISTRIERUNG
    // ════════════════════════════════════════
    public AuthResponse register(RegisterRequest request) {

        // Prüfen ob Username/Email bereits vergeben
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Benutzername bereits vergeben");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email bereits registriert");
        }

        // User-Entity erstellen (Builder Pattern)
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // ↑ Passwort IMMER hashen! Niemals Klartext speichern!
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.Role.USER)
                .build();

        // In Datenbank speichern
        User savedUser = userRepository.save(user);

        // JWT Token generieren
        String token = jwtUtil.generateToken(savedUser);

        // Response aufbauen und zurückgeben
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    // ════════════════════════════════════════
    // LOGIN
    // ════════════════════════════════════════
    public AuthResponse login(LoginRequest request) {

        // Spring Security kümmert sich um die Authentifizierung
        // Wirft BadCredentialsException wenn Username/Passwort falsch
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // ↑ Wenn wir hier ankommen: Credentials waren korrekt!

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}