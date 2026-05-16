// src/main/java/com/taskmanager/backend/dto/RegisterRequest.java

package com.taskmanager.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100, message = "Passwort muss mindestens 6 Zeichen haben")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}