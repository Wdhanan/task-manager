// src/main/java/com/taskmanager/backend/security/JwtAuthFilter.java
// Dieser Filter wird bei JEDEM HTTP-Request ausgeführt

package com.taskmanager.backend.security;

import com.taskmanager.backend.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor  // ← Lombok: Konstruktor für alle 'final' Felder
@Slf4j  // ← Lombok: erstellt automatisch einen Logger
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Kein Token → einfach weitermachen (Login/Register brauchen keins)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            // ── Token lesen und User authentifizieren ──
            final String username = jwtUtil.extractUsername(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            // ✅ Token abgelaufen → NICHT als Fehler behandeln!
            // Einfach ignorieren → Request läuft weiter als "nicht authentifiziert"
            // Spring Security entscheidet dann:
            // - Öffentliche Route (/auth/login) → OK, durchlassen
            // - Geschützte Route → 401 zurückgeben
            log.debug("JWT Token abgelaufen: {}", e.getMessage());

        } catch (MalformedJwtException e) {
            // Token hat falsches Format (manipuliert?)
            log.debug("Ungültiges JWT Token Format: {}", e.getMessage());

        } catch (SignatureException e) {
            // Token-Signatur stimmt nicht (manipuliert!)
            log.debug("Ungültige JWT Signatur: {}", e.getMessage());

        } catch (Exception e) {
            // Alle anderen Token-Fehler
            log.debug("JWT Verarbeitungsfehler: {}", e.getMessage());
        }

        // In ALLEN Fällen: Request weiterschicken
        filterChain.doFilter(request, response);
    }
}