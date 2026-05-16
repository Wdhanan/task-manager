// src/main/java/com/taskmanager/backend/security/JwtUtil.java

package com.taskmanager.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component  // ← "Spring, verwalte diese Klasse" (kann per @Autowired injiziert werden)
public class JwtUtil {

    @Value("${app.jwt.secret}")
    // ↑ Liest den Wert aus application.yml: app.jwt.secret
    private String secret;

    @Value("${app.jwt.expiration}")
    private long expiration;  // 86400000 ms = 24h

    // Den Schlüssel aus dem Secret erstellen
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
        // ↑ HMAC-SHA256 Schlüssel aus unserem Secret-String
    }

    // ════════════════════════════════════════
    // TOKEN ERSTELLEN
    // ════════════════════════════════════════
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Hier können wir zusätzliche Daten in den Token packen:
        // extraClaims.put("role", userDetails.getAuthorities());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                // ↑ "subject" = wer ist dieser Token? (Username)
                .issuedAt(new Date())
                // ↑ "Erstellt jetzt"
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // ↑ "Läuft ab in 24 Stunden"
                .signWith(getSigningKey())
                // ↑ Token mit unserem Schlüssel signieren
                .compact();
        // ↑ Alles zusammenbauen → String "eyJ..."
    }

    // ════════════════════════════════════════
    // USERNAME AUS TOKEN LESEN
    // ════════════════════════════════════════
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ════════════════════════════════════════
    // TOKEN VALIDIEREN
    // ════════════════════════════════════════
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            // Token manipuliert, abgelaufen, falsches Format → ungültig
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                // ↑ Prüft die Signatur – wenn jemand den Token manipuliert hat, schlägt das fehl
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}