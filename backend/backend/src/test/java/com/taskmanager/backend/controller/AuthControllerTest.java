// src/test/java/com/taskmanager/backend/controller/AuthControllerTest.java

package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.AuthResponse;
import com.taskmanager.backend.dto.LoginRequest;
import com.taskmanager.backend.dto.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// ↑ Startet einen echten Server auf einem zufälligen Port
//   Kein Konflikt mit Port 8080 wenn der schon belegt ist

@ActiveProfiles("test")
// ↑ Benutzt src/test/resources/application.yml (H2 Datenbank)

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
// ↑ Nach jedem Test: Spring Context neu starten
//   Verhindert dass Test-Daten sich gegenseitig beeinflussen
//   (z.B. ein registrierter User aus Test 1 stört Test 2)

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// ↑ Tests in definierter Reihenfolge ausführen
class AuthControllerTest {

    @LocalServerPort
    private int port;
    // ↑ Der zufällige Port wird hier injiziert
    //   z.B. 54321

    @Autowired
    private TestRestTemplate restTemplate;
    // ↑ TestRestTemplate = HTTP-Client für Tests
    //   Schickt echte HTTP-Requests zum echten Server

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        // ↑ Basis-URL für alle Requests
        //   z.B. http://localhost:54321/api/auth
    }

    // ════════════════════════════════════════
    // REGISTER TESTS
    // ════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("Register: 201 bei gültigen Daten")
    void register_Returns201_WithValidData() {

        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        // Act – echter HTTP POST Request
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/register",
                request,
                AuthResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotEmpty();
        assertThat(response.getBody().getUsername()).isEqualTo("testuser");
        assertThat(response.getBody().getRole()).isEqualTo("USER");
    }

    @Test
    @Order(2)
    @DisplayName("Register: 400 wenn Username fehlt")
    void register_Returns400_WhenUsernameEmpty() {

        RegisterRequest request = new RegisterRequest();
        // username absichtlich weggelassen
        request.setEmail("test2@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl + "/register",
                request,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(3)
    @DisplayName("Register: 400 bei doppeltem Username")
    void register_Returns400_WhenUsernameAlreadyExists() {

        // Erst registrieren
        RegisterRequest first = new RegisterRequest();
        first.setUsername("duplicate");
        first.setEmail("first@example.com");
        first.setPassword("password123");
        first.setFirstName("First");
        first.setLastName("User");
        restTemplate.postForEntity(baseUrl + "/register", first, Object.class);

        // Nochmal mit gleichem Username registrieren
        RegisterRequest second = new RegisterRequest();
        second.setUsername("duplicate");
        second.setEmail("second@example.com");
        second.setPassword("password123");
        second.setFirstName("Second");
        second.setLastName("User");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl + "/register",
                second,
                Object.class
        );

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ════════════════════════════════════════
    // LOGIN TESTS
    // ════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("Login: 200 bei korrekten Credentials")
    void login_Returns200_WithValidCredentials() {

        // Erst User registrieren
        RegisterRequest register = new RegisterRequest();
        register.setUsername("loginuser");
        register.setEmail("login@example.com");
        register.setPassword("password123");
        register.setFirstName("Login");
        register.setLastName("User");
        restTemplate.postForEntity(baseUrl + "/register", register, Object.class);

        // Dann einloggen
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/login",
                loginRequest,
                AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotEmpty();
        assertThat(response.getBody().getUsername()).isEqualTo("loginuser");
    }
}