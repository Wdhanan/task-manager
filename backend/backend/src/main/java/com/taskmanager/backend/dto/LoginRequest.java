// src/main/java/com/taskmanager/backend/dto/LoginRequest.java

package com.taskmanager.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data  // ← Lombok: Getter + Setter + toString + equals + hashCode
public class LoginRequest {

    @NotBlank(message = "Benutzername ist erforderlich")
    private String username;

    @NotBlank(message = "Passwort ist erforderlich")
    private String password;
}