// src/test/java/com/taskmanager/backend/service/TaskServiceTest.java

package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.TaskDto;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TaskService taskService;

    // Hilfsobjekte die wir in mehreren Tests brauchen
    private User testUser;
    private Task testTask;

    @BeforeEach  // ← Wird VOR JEDEM Test ausgeführt
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.USER)
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Mein Test Task")
                .description("Beschreibung")
                .status(Task.Status.TODO)
                .priority(Task.Priority.MEDIUM)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("getTasks gibt alle Tasks des Users zurück")
    void getTasks_ReturnsAllUserTasks_WhenNoFilter() {

        // Arrange
        List<Task> tasks = List.of(testTask,
                Task.builder().id(2L).title("Task 2").status(Task.Status.DONE)
                        .priority(Task.Priority.HIGH).user(testUser).build()
        );
        when(taskRepository.findByUserId(1L)).thenReturn(tasks);

        // Act
        List<TaskDto> result = taskService.getTasks(1L, null, null);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Mein Test Task");
        assertThat(result.get(1).getTitle()).isEqualTo("Task 2");
    }

    @Test
    @DisplayName("createTask erstellt und speichert Task korrekt")
    void createTask_CreatesAndSavesTask_Successfully() {

        // Arrange
        TaskDto inputDto = new TaskDto();
        inputDto.setTitle("Neuer Task");
        inputDto.setDescription("Neue Beschreibung");
        inputDto.setStatus("TODO");
        inputDto.setPriority("HIGH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Task savedTask = Task.builder()
                .id(5L)
                .title("Neuer Task")
                .description("Neue Beschreibung")
                .status(Task.Status.TODO)
                .priority(Task.Priority.HIGH)
                .user(testUser)
                .build();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        TaskDto result = taskService.createTask(inputDto, 1L);

        // Assert
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("Neuer Task");
        assertThat(result.getPriority()).isEqualTo("HIGH");

        // Argument Captor: Was wurde genau an save() übergeben?
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        // ↑ "Fange das Argument das an save() übergeben wurde"

        Task capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getTitle()).isEqualTo("Neuer Task");
        assertThat(capturedTask.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("getTask wirft Exception wenn Task nicht existiert")
    void getTask_ThrowsResourceNotFoundException_WhenTaskNotFound() {

        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.getTask(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deleteTask wirft Exception wenn Task anderem User gehört")
    void deleteTask_ThrowsAccessDenied_WhenTaskBelongsToDifferentUser() {

        // Arrange - Task gehört User 1, aber User 2 versucht zu löschen
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act & Assert
        assertThatThrownBy(() -> taskService.deleteTask(1L, 999L))
                // ↑ User 999 versucht Task von User 1 zu löschen
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

        // Task darf NICHT gelöscht worden sein
        verify(taskRepository, never()).delete(any());
    }
}