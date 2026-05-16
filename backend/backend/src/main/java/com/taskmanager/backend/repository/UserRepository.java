// src/main/java/com/taskmanager/backend/repository/UserRepository.java

package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
// JpaRepository<User, Long> = "Repository für User-Entities mit Long als ID-Typ"
// Gibt uns kostenlos: save(), findById(), findAll(), deleteById(), count(), etc.
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring leitet das SQL aus dem Methodennamen ab:
    // findBy → SELECT * FROM users WHERE
    // Username → username =
    Optional<User> findByUsername(String username);
    // → SELECT * FROM users WHERE username = ?

    Optional<User> findByEmail(String email);
    // → SELECT * FROM users WHERE email = ?

    boolean existsByUsername(String username);
    // → SELECT COUNT(*) > 0 FROM users WHERE username = ?

    boolean existsByEmail(String email);
    // → SELECT COUNT(*) > 0 FROM users WHERE email = ?
}