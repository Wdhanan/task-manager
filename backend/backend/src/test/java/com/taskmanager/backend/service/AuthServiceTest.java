// src/test/java/com/taskmanager/backend/service/AuthServiceTest.java

package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.LoginRequest;
import com.taskmanager.backend.dto.RegisterRequest;
import com.taskmanager.backend.dto.AuthResponse;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
//   Viel schneller als @SpringBootTest oder @WebMvcTest
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Register: Erfolgreich wenn User nicht existiert")
    void register_Success() {

        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$hashedPw");

        User savedUser = User.builder()
                .id(1L).username("newuser")
                .email("new@example.com")
                .role(User.Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any())).thenReturn("jwt.token.here");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getUsername()).isEqualTo("newuser");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register: Fehler wenn Username bereits existiert")
    void register_Fails_WhenUsernameExists() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("email@example.com");
        request.setPassword("pw");
        request.setFirstName("A");
        request.setLastName("B");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Benutzername bereits vergeben");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login: Erfolgreich bei korrekten Credentials")
    void login_Success() {

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L).username("testuser")
                .email("test@example.com")
                .role(User.Role.USER)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("login.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("login.jwt.token");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Login: Fehler bei falschen Credentials")
    void login_Fails_WithWrongCredentials() {

        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("wrongpw");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Falsch"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}