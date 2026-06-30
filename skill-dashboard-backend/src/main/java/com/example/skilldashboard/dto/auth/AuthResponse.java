package com.example.skilldashboard.dto.auth;

import com.example.skilldashboard.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long accessTokenExpiresIn;

    private long refreshTokenExpiresIn;

    private Long userId;

    private String name;

    private String email;

    private Role role;
}