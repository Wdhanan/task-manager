// src/main/java/com/taskmanager/backend/controller/TaskController.java

package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.TaskDto;
import com.taskmanager.backend.entity.User;
import com.taskmanager.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // GET /api/tasks
    // GET /api/tasks?status=TODO
    // GET /api/tasks?status=TODO&priority=HIGH
    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @AuthenticationPrincipal User currentUser,
            // ↑ "Gib mir den aktuell eingeloggten User"
            //   Spring Security holt ihn aus dem SecurityContext (den wir in JwtAuthFilter setzen)
            @RequestParam(required = false) String status,
            // ↑ @RequestParam = URL-Parameter (?status=TODO)
            //   required=false = Optional, kann auch fehlen
            @RequestParam(required = false) String priority
    ) {
        List<TaskDto> tasks = taskService.getTasks(currentUser.getId(), status, priority);
        return ResponseEntity.ok(tasks);
    }

    // GET /api/tasks/5
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(
            @PathVariable Long id,
            // ↑ @PathVariable = Teil der URL (/tasks/{id})
            @AuthenticationPrincipal User currentUser
    ) {
        TaskDto task = taskService.getTask(id, currentUser.getId());
        return ResponseEntity.ok(task);
    }

    // POST /api/tasks
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody TaskDto taskDto,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskDto created = taskService.createTask(taskDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/tasks/5
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDto taskDto,
            @AuthenticationPrincipal User currentUser
    ) {
        TaskDto updated = taskService.updateTask(id, taskDto, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/tasks/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        taskService.deleteTask(id, currentUser.getId());
        return ResponseEntity.noContent().build();
        // ↑ HTTP 204 No Content (erfolgreich gelöscht, kein Body)
    }

    // GET /api/tasks/statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(taskService.getStatistics(currentUser.getId()));
    }
}