// src/main/java/com/taskmanager/backend/service/TaskService.java

package com.taskmanager.backend.service;

import com.taskmanager.backend.dto.TaskDto;
import com.taskmanager.backend.entity.*;
import com.taskmanager.backend.exception.ResourceNotFoundException;
import com.taskmanager.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // ════════════════════════════════════════
    // ALLE TASKS HOLEN (mit optionalem Filter)
    // ════════════════════════════════════════
    public List<TaskDto> getTasks(Long userId, String status, String priority) {

        List<Task> tasks;

        // Entscheide welche Repository-Methode basierend auf Filtern
        if (status != null && priority != null) {
            tasks = taskRepository.findByUserIdAndStatusAndPriority(
                    userId,
                    Task.Status.valueOf(status),      // String → Enum
                    Task.Priority.valueOf(priority)
            );
        } else if (status != null) {
            tasks = taskRepository.findByUserIdAndStatus(userId, Task.Status.valueOf(status));
        } else if (priority != null) {
            tasks = taskRepository.findByUserIdAndPriority(userId, Task.Priority.valueOf(priority));
        } else {
            tasks = taskRepository.findByUserId(userId);
        }

        // Entity-Liste → DTO-Liste umwandeln
        // .stream() = "Verarbeite jedes Element"
        // .map() = "Wandle es um"
        // .collect() = "Sammle alles wieder in eine Liste"
        return tasks.stream()
                .map(TaskDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ════════════════════════════════════════
    // EINEN TASK HOLEN
    // ════════════════════════════════════════
    public TaskDto getTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task nicht gefunden: " + taskId));

        // Sicherheitsprüfung: Gehört dieser Task diesem User?
        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Zugriff verweigert");
        }

        return TaskDto.fromEntity(task);
    }

    // ════════════════════════════════════════
    // TASK ERSTELLEN
    // ════════════════════════════════════════
    @Transactional
    // ↑ "Alles oder nichts" - wenn ein Fehler passiert, wird alles zurückgerollt
    public TaskDto createTask(TaskDto taskDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User nicht gefunden"));

        Task task = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(taskDto.getStatus() != null
                        ? Task.Status.valueOf(taskDto.getStatus())
                        : Task.Status.TODO)
                .priority(taskDto.getPriority() != null
                        ? Task.Priority.valueOf(taskDto.getPriority())
                        : Task.Priority.MEDIUM)
                .dueDate(taskDto.getDueDate())
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        return TaskDto.fromEntity(saved);
    }

    // ════════════════════════════════════════
    // TASK AKTUALISIEREN
    // ════════════════════════════════════════
    @Transactional
    public TaskDto updateTask(Long taskId, TaskDto taskDto, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task nicht gefunden"));

        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Zugriff verweigert");
        }

        // Nur nicht-null Felder aktualisieren
        if (taskDto.getTitle()       != null) task.setTitle(taskDto.getTitle());
        if (taskDto.getDescription() != null) task.setDescription(taskDto.getDescription());
        if (taskDto.getStatus()      != null) task.setStatus(Task.Status.valueOf(taskDto.getStatus()));
        if (taskDto.getPriority()    != null) task.setPriority(Task.Priority.valueOf(taskDto.getPriority()));
        if (taskDto.getDueDate()     != null) task.setDueDate(taskDto.getDueDate());

        Task updated = taskRepository.save(task);
        return TaskDto.fromEntity(updated);
    }

    // ════════════════════════════════════════
    // TASK LÖSCHEN
    // ════════════════════════════════════════
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task nicht gefunden"));

        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Zugriff verweigert");
        }

        taskRepository.delete(task);
    }

    // Dashboard-Statistiken
    public java.util.Map<String, Long> getStatistics(Long userId) {
        return java.util.Map.of(
                "total",      taskRepository.countByUserIdAndStatus(userId, Task.Status.TODO)
                        + taskRepository.countByUserIdAndStatus(userId, Task.Status.IN_PROGRESS)
                        + taskRepository.countByUserIdAndStatus(userId, Task.Status.DONE),
                "todo",       taskRepository.countByUserIdAndStatus(userId, Task.Status.TODO),
                "inProgress", taskRepository.countByUserIdAndStatus(userId, Task.Status.IN_PROGRESS),
                "done",       taskRepository.countByUserIdAndStatus(userId, Task.Status.DONE)
        );
    }
}