// src/test/java/com/taskmanager/backend/controller/AuthControllerTest.java

package com.taskmanager.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.backend.dto.*;
import com.taskmanager.backend.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest = Nur den Web-Layer testen (Controller + Security)
// Kein Service, kein Repository, kein echtes Spring Context
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    // ↑ MockMvc = "Simuliert HTTP-Requests ohne echten Server"

    @Autowired
    private ObjectMapper objectMapper;
    // ↑ ObjectMapper = JSON ↔ Java Objekt Konverter

    @MockBean
    private AuthService authService;
    // ↑ @MockBean = Mock der in den Spring Context injiziert wird

    @Test
    @DisplayName("POST /api/auth/register → 201 Created bei gültigem Request")
    void register_Returns201_WithValidRequest() throws Exception {

        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("Max");
        request.setLastName("Muster");

        AuthResponse mockResponse = AuthResponse.builder()
                .token("mock.token.here")
                .id(1L)
                .username("newuser")
                .email("new@example.com")
                .role("USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(
                        post("/api/auth/register")             // HTTP POST Request
                                .contentType(MediaType.APPLICATION_JSON) // Content-Type Header
                                .content(objectMapper.writeValueAsString(request))
                        // ↑ Java Objekt → JSON String
                )
                .andExpect(status().isCreated())           // HTTP 201?
                .andExpect(jsonPath("$.token").value("mock.token.here"))
                // ↑ jsonPath = JSON-Pfad in der Antwort prüfen
                //   "$.token" = Feld "token" im Root-Objekt
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 Bad Request bei fehlendem Username")
    void register_Returns400_WhenUsernameIsMissing() throws Exception {

        // Arrange - Request OHNE Username
        RegisterRequest request = new RegisterRequest();
        // request.setUsername() ← absichtlich nicht gesetzt!
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Max");
        request.setLastName("Muster");

        // Act & Assert
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())  // HTTP 400?
                .andExpect(jsonPath("$.fieldErrors.username").exists());
        // ↑ Fehlermeldung für "username" Feld vorhanden?
    }

    @Test
    @DisplayName("POST /api/auth/login → 401 bei falschen Credentials")
    void login_Returns401_WithInvalidCredentials() throws Exception {

        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("johndoe");
        request.setPassword("falschesPasswort");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Ungültige Credentials"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());  // HTTP 401?
    }
}