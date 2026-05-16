// src/main/java/com/taskmanager/backend/repository/TaskRepository.java

package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Alle Tasks eines Users
    List<Task> findByUserId(Long userId);

    // Tasks nach User UND Status filtern
    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);

    // Tasks nach User UND Priorität filtern
    List<Task> findByUserIdAndPriority(Long userId, Task.Priority priority);

    // Tasks nach User UND Status UND Priorität filtern
    List<Task> findByUserIdAndStatusAndPriority(
            Long userId,
            Task.Status status,
            Task.Priority priority
    );

    // Manuelle JPQL Query (Java Persistence Query Language)
    // Für komplexere Abfragen die sich nicht durch Methodennamen ausdrücken lassen
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    // ↑ JPQL nutzt Klassenname (Task) und Feldnamen, nicht Tabellennamen
    List<Task> searchByUserIdAndText(
            @Param("userId") Long userId,
            @Param("search") String search
    );

    // Zählt Tasks eines Users nach Status
    long countByUserIdAndStatus(Long userId, Task.Status status);
}