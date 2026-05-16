// src/test/java/com/taskmanager/backend/repository/TaskRepositoryTest.java

package com.taskmanager.backend.repository;

import com.taskmanager.backend.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

// @DataJpaTest = Nur JPA/Repository Layer testen
// Nutzt automatisch H2 In-Memory Datenbank
// Jeder Test läuft in einer Transaktion die automatisch zurückgerollt wird
@DataJpaTest
class TaskRepositoryTest {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        // Testuser in H2 Datenbank erstellen
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("$2a$10$hashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .build();
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("findByUserId gibt nur Tasks des angegebenen Users zurück")
    void findByUserId_ReturnsOnlyUserTasks() {

        // Arrange - 2 Tasks für testUser, 1 für anderen User
        Task task1 = Task.builder()
                .title("Task 1")
                .status(Task.Status.TODO)
                .priority(Task.Priority.LOW)
                .user(savedUser)
                .build();

        Task task2 = Task.builder()
                .title("Task 2")
                .status(Task.Status.DONE)
                .priority(Task.Priority.HIGH)
                .user(savedUser)
                .build();

        taskRepository.save(task1);
        taskRepository.save(task2);

        // Act
        List<Task> foundTasks = taskRepository.findByUserId(savedUser.getId());

        // Assert
        assertThat(foundTasks).hasSize(2);
        assertThat(foundTasks)
                .extracting(Task::getTitle)  // Nur die Titel extrahieren
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("findByUserIdAndStatus filtert korrekt nach Status")
    void findByUserIdAndStatus_FiltersCorrectly() {

        // Arrange
        taskRepository.save(Task.builder().title("Todo 1").status(Task.Status.TODO)
                .priority(Task.Priority.LOW).user(savedUser).build());
        taskRepository.save(Task.builder().title("Todo 2").status(Task.Status.TODO)
                .priority(Task.Priority.HIGH).user(savedUser).build());
        taskRepository.save(Task.builder().title("Done 1").status(Task.Status.DONE)
                .priority(Task.Priority.MEDIUM).user(savedUser).build());

        // Act
        List<Task> todoTasks = taskRepository.findByUserIdAndStatus(
                savedUser.getId(), Task.Status.TODO
        );

        // Assert
        assertThat(todoTasks).hasSize(2);
        assertThat(todoTasks).allMatch(t -> t.getStatus() == Task.Status.TODO);
        // ↑ allMatch = Alle Elemente müssen diese Bedingung erfüllen
    }
}