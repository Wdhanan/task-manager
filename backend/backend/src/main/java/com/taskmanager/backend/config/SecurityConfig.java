// src/main/java/com/taskmanager/backend/config/SecurityConfig.java

package com.taskmanager.backend.config;

import com.taskmanager.backend.security.*;
import com.taskmanager.backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration       // ← "Diese Klasse enthält Spring-Konfiguration"
@EnableWebSecurity   // ← "Aktiviere Spring Security"
@EnableMethodSecurity // ← Ermöglicht @PreAuthorize auf Methoden
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ════════════════════════════════════════
    // PASSWORT VERSCHLÜSSELUNG
    // ════════════════════════════════════════
    @Bean  // ← "@Bean" = "Spring, verwalte dieses Objekt in deinem Container"
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // ↑ BCrypt = Sicherer Hashing-Algorithmus
        //   "geheim" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        //   Jedes Mal anders (Salt)! Kann nicht rückgängig gemacht werden.
    }

    // ════════════════════════════════════════
    // AUTHENTICATION PROVIDER
    // ════════════════════════════════════════
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        // ↑ "Lade User aus der Datenbank"
        provider.setPasswordEncoder(passwordEncoder());
        // ↑ "Vergleiche Passwörter mit BCrypt"
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ════════════════════════════════════════
    // CORS KONFIGURATION
    // ════════════════════════════════════════
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",   // ← Angular Dev Server
                "http://localhost:80"      // ← Angular in Docker
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // ↑ "Diese CORS-Regeln gelten für ALLE Pfade"
        return source;
    }

    // ════════════════════════════════════════
    // SECURITY FILTER CHAIN (Hauptkonfiguration)
    // ════════════════════════════════════════
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deaktivieren (nicht nötig bei JWT/Stateless APIs)
                .csrf(csrf -> csrf.disable())

                // CORS aktivieren
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Welche URLs brauchen Login?
                .authorizeHttpRequests(auth -> auth
                        // Diese Pfade sind ÖFFENTLICH (kein Token nötig)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // ALLE anderen Pfade brauchen einen gültigen Token
                        .anyRequest().authenticated()
                )

                // Session-Management: STATELESS
                // ↑ Spring soll KEINE Sessions speichern (JWT macht das überflüssig)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authentication Provider registrieren
                .authenticationProvider(authenticationProvider())

                // Unseren JWT Filter VOR dem Standard-Filter einbauen
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // ↑ Reihenfolge: JwtAuthFilter → UsernamePasswordAuthenticationFilter → Controller

        return http.build();
    }
}