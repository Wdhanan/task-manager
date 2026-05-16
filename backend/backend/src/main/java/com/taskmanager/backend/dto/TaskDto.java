// src/main/java/com/taskmanager/backend/dto/TaskDto.java

package com.taskmanager.backend.dto;

import com.taskmanager.backend.entity.Task;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long id;

    @NotBlank(message = "Titel ist erforderlich")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    private String status;    // "TODO", "IN_PROGRESS", "DONE"
    private String priority;  // "LOW", "MEDIUM", "HIGH"
    private LocalDate dueDate;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statische Hilfsmethode: Task-Entity → TaskDto umwandeln
    public static TaskDto fromEntity(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .userId(task.getUser().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}