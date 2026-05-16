// src/main/java/com/taskmanager/backend/dto/AuthResponse.java

package com.taskmanager.backend.dto;

import lombok.*;

@Data
@Builder           // ← Ermöglicht: AuthResponse.builder().token("...").build()
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;
}