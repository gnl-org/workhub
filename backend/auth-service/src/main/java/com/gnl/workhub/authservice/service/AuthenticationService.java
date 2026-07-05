package com.gnl.workhub.authservice.service;

import com.gnl.workhub.authservice.config.UserEventPublisher;
import com.gnl.workhub.authservice.dto.*;
import com.gnl.workhub.authservice.entity.User;
import com.gnl.workhub.authservice.enums.UserRole;
import com.gnl.workhub.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.gnl.workhub.authservice.config.JwtService;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserEventPublisher eventPublisher;

    public TokenResponse register(RegisterRequest request) {
        var user = new User(request.email(), passwordEncoder.encode(request.password()), request.fullName(), UserRole.USER);
        repository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        eventPublisher.publishUserCreated(new UserEvent(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getGlobalRole().name(), "USER_CREATED"
        ));

        return new TokenResponse(accessToken, refreshToken);
    }

    public AuthResult authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = repository.findByEmail(request.email()).orElseThrow();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResult(
                new TokenResponse(accessToken, refreshToken),
                new UserResponse(user.getId(), user.getEmail(), user.getGlobalRole().name(), user.getFullName())
        );
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);
        if (email == null) throw new RuntimeException("Invalid refresh token claims");

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        return new TokenResponse(jwtService.generateToken(user), jwtService.generateRefreshToken(user));
    }

    public UserResponse getMe(String email) {
        var user = repository.findByEmail(email).orElseThrow();
        return new UserResponse(user.getId(), user.getEmail(), user.getGlobalRole().name(), user.getFullName());
    }
}
