package com.example.skilldashboard.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;


@Data
public class SignupRequest {

    @Schema(example = "Jay Sharma", description = "Full name of the user")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(example = "jay@gmail.com", description = "Unique email address")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;


    @Schema(example = "Password@123", description = "User password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}