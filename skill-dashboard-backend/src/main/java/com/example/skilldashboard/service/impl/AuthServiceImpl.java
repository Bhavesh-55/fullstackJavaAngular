package com.example.skilldashboard.service.impl;

import com.example.skilldashboard.dto.auth.AuthResponse;
import com.example.skilldashboard.dto.auth.LoginRequest;
import com.example.skilldashboard.dto.auth.SignupRequest;
import com.example.skilldashboard.model.RefreshToken;
import com.example.skilldashboard.model.Role;
import com.example.skilldashboard.model.User;
import com.example.skilldashboard.repository.RefreshTokenRepository;
import com.example.skilldashboard.repository.UserRepository;
import com.example.skilldashboard.security.JwtService;
import com.example.skilldashboard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        return createTokenResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        return createTokenResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        String tokenHash = jwtService.hashRefreshToken(refreshToken);

        RefreshToken savedRefreshToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (savedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            savedRefreshToken.setRevoked(true);
            savedRefreshToken.setRevokedAt(LocalDateTime.now());

            throw new IllegalArgumentException("Refresh token expired");
        }

        User user = savedRefreshToken.getUser();

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("User account is disabled");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtService.getAccessTokenExpirationMs())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationMs())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        String tokenHash = jwtService.hashRefreshToken(refreshToken);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(savedToken -> {
                    savedToken.setRevoked(true);
                    savedToken.setRevokedAt(LocalDateTime.now());
                });
    }

    private AuthResponse createTokenResponse(User user) {
        // Optional: one active refresh token per user.
        // This means login from new device logs out old session.
        refreshTokenRepository.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        RefreshToken savedRefreshToken = RefreshToken.builder()
                .tokenHash(jwtService.hashRefreshToken(refreshToken))
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(savedRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtService.getAccessTokenExpirationMs())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationMs())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}