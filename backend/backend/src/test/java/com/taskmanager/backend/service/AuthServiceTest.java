// src/test/java/com/taskmanager/backend/service/AuthServiceTest.java

package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.*;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// @ExtendWith = "Benutze Mockito für diese Test-Klasse"
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  // @Mock = "Erstelle einen Fake dieser Klasse"
  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;
  @Mock private AuthenticationManager authenticationManager;

  // @InjectMocks = "Erstelle AuthService und injiziere alle @Mocks"
  @InjectMocks
  private AuthService authService;

  // ════════════════════════════════════════
  // REGISTER TESTS
  // ════════════════════════════════════════

  @Test
  // @Test = "Das ist ein Test"
  @DisplayName("Registrierung erfolgreich wenn Username und Email verfügbar")
  // @DisplayName = Lesbarer Name im Test-Report
  void register_Success_WhenUsernameAndEmailAvailable() {

    // ── ARRANGE (Vorbereitung) ──────────────────────────────
    // "Gegeben" = Was ist der Ausgangszustand?

    RegisterRequest request = new RegisterRequest();
    request.setUsername("johndoe");
    request.setEmail("john@example.com");
    request.setPassword("password123");
    request.setFirstName("John");
    request.setLastName("Doe");

    // Mock-Verhalten definieren:
    // "Wenn userRepository.existsByUsername('johndoe') aufgerufen wird → gib false zurück"
    when(userRepository.existsByUsername("johndoe")).thenReturn(false);
    when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

    // "Wenn passwordEncoder.encode(irgendein String) aufgerufen wird → gib das zurück"
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

    // "Wenn userRepository.save(irgendein User) aufgerufen wird → gib diesen User zurück"
    User savedUser = User.builder()
        .id(1L)
        .username("johndoe")
        .email("john@example.com")
        .password("$2a$10$hashedPassword")
        .firstName("John")
        .lastName("Doe")
        .role(User.Role.USER)
        .build();
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtUtil.generateToken(any())).thenReturn("mock.jwt.token");

    // ── ACT (Ausführung) ─────────────────────────────────────
    // "Wenn" = Die eigentliche Aktion
    AuthResponse response = authService.register(request);

    // ── ASSERT (Prüfung) ─────────────────────────────────────
    // "Dann" = Was soll das Ergebnis sein?

    // AssertJ macht lesbare Assertions:
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("mock.jwt.token");
    assertThat(response.getUsername()).isEqualTo("johndoe");
    assertThat(response.getEmail()).isEqualTo("john@example.com");
    assertThat(response.getRole()).isEqualTo("USER");

    // Prüfen ob die Methoden aufgerufen wurden:
    verify(userRepository).save(any(User.class));
    // ↑ "userRepository.save() muss genau einmal aufgerufen worden sein"

    verify(passwordEncoder).encode("password123");
    // ↑ "Das Passwort muss gehasht worden sein"
  }

  @Test
  @DisplayName("Registrierung schlägt fehl wenn Username bereits vergeben")
  void register_ThrowsException_WhenUsernameAlreadyExists() {

    // Arrange
    RegisterRequest request = new RegisterRequest();
    request.setUsername("existing_user");
    request.setEmail("new@example.com");
    request.setPassword("password");
    request.setFirstName("Max");
    request.setLastName("Muster");

    when(userRepository.existsByUsername("existing_user")).thenReturn(true);
    // ↑ Mock sagt: "Dieser Username existiert bereits"

    // Act & Assert kombiniert:
    // "assertThatThrownBy" = "Dieser Code-Block muss eine Exception werfen"
    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Benutzername bereits vergeben");

    // Prüfen dass save() NICHT aufgerufen wurde (kein User gespeichert)
    verify(userRepository, never()).save(any());
    // ↑ never() = "Diese Methode darf NICHT aufgerufen worden sein"
  }

  // ════════════════════════════════════════
  // LOGIN TESTS
  // ════════════════════════════════════════

  @Test
  @DisplayName("Login erfolgreich bei korrekten Credentials")
  void login_Success_WithValidCredentials() {

    // Arrange
    LoginRequest request = new LoginRequest();
    request.setUsername("johndoe");
    request.setPassword("password123");

    User user = User.builder()
        .id(1L)
        .username("johndoe")
        .email("john@example.com")
        .role(User.Role.USER)
        .build();

    // authenticationManager wirft keine Exception = Credentials korrekt
    when(authenticationManager.authenticate(any())).thenReturn(null);
    when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(user)).thenReturn("valid.jwt.token");

    // Act
    AuthResponse response = authService.login(request);

    // Assert
    assertThat(response.getToken()).isEqualTo("valid.jwt.token");
    assertThat(response.getUsername()).isEqualTo("johndoe");
  }

  @Test
  @DisplayName("Login schlägt fehl bei falschen Credentials")
  void login_ThrowsException_WithInvalidCredentials() {

    // Arrange
    LoginRequest request = new LoginRequest();
    request.setUsername("johndoe");
    request.setPassword("falschesPassword");

    // authenticationManager wirft Exception = Credentials falsch
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Ungültige Credentials"));

    // Act & Assert
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(BadCredentialsException.class);
  }
}