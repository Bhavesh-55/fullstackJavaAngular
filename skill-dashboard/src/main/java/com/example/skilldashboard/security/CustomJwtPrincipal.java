package com.example.skilldashboard.security;

import com.example.skilldashboard.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomJwtPrincipal {

    private Long userId;

    private String name;

    private String email;

    private Role role;
}