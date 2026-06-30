package com.example.skilldashboard.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class LoginRequest {

    @Schema(example = "jay@gmail.com", description = "Registered email address")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;


    @Schema(example = "Password@123", description = "User password")
    @NotBlank(message = "Password is required")
    private String password;
}