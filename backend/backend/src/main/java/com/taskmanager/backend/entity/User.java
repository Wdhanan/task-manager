// src/main/java/com/taskmanager/backend/entity/User.java

package com.taskmanager.backend.entity;

import jakarta.persistence.*;           // JPA Annotationen
import jakarta.validation.constraints.*; // Validierungsregeln
import lombok.*;                        // Getter, Setter, etc. automatisch
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity                          // ← "Diese Klasse = Datenbanktabelle"
@Table(name = "users")           // ← Tabellenname in der DB
@Data                            // ← Lombok: erstellt Getter, Setter, toString, equals, hashCode
@NoArgsConstructor               // ← Lombok: erstellt leeren Konstruktor
@AllArgsConstructor              // ← Lombok: erstellt Konstruktor mit ALLEN Feldern
@Builder                         // ← Lombok: ermöglicht Builder-Pattern (kommt gleich)
public class User implements UserDetails {
// UserDetails = Spring Security Interface: "Diese Klasse repräsentiert einen User"

    @Id                            // ← "Das ist der Primärschlüssel"
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ↑ "ID automatisch hochzählen" (1, 2, 3, ...)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    // ↑ unique=kein doppelter Wert, nullable=Pflichtfeld, length=max 50 Zeichen
    @NotBlank(message = "Benutzername ist erforderlich")
    @Size(min = 3, max = 50)
    private String username;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email ist erforderlich")
    @Email(message = "Ungültige Email-Adresse")
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String password;       // ← Wird IMMER gehasht gespeichert (niemals Klartext!)

    @Column(nullable = false, length = 30)
    private String firstName;

    @Column(nullable = false, length = 30)
    private String lastName;

    @Enumerated(EnumType.STRING)   // ← Speichert "USER" oder "ADMIN" als Text in DB
    @Column(nullable = false)
    private Role role = Role.USER; // ← Standardwert: USER

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Tasks des Users (One User hat MANY Tasks)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // ↑ mappedBy="user" = "Das 'user' Feld in Task verwaltet diese Beziehung"
    // cascade=ALL = "Wenn User gelöscht → Tasks auch löschen"
    // fetch=LAZY = "Tasks erst laden wenn sie wirklich gebraucht werden" (Performance!)
    private List<Task> tasks;

    // Wird automatisch vor dem ersten Speichern aufgerufen
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // Wird automatisch vor jedem Update aufgerufen
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════
    // UserDetails Interface Methoden (Spring Security)
    // ═══════════════════════════════════════════

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Gibt die Rolle als Spring Security "Authority" zurück
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        // → z.B. "ROLE_USER" oder "ROLE_ADMIN"
    }

    @Override
    public boolean isAccountNonExpired()    { return true; }
    @Override
    public boolean isAccountNonLocked()     { return true; }
    @Override
    public boolean isCredentialsNonExpired(){ return true; }
    @Override
    public boolean isEnabled()              { return true; }

    // Enum innerhalb der Klasse definieren
    public enum Role {
        USER, ADMIN
    }
}