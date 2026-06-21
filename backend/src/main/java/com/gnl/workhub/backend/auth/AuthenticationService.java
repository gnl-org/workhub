package com.gnl.workhub.backend.auth;

import lombok.RequiredArgsConstructor;
import com.gnl.workhub.backend.repository.UserRepository;
import com.gnl.workhub.backend.service.JwtService;
import com.gnl.workhub.backend.entity.User;
import com.gnl.workhub.backend.enums.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public TokenResponse register(RegisterRequest request) {
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .globalRole(UserRole.USER)
                .build();
        repository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResult authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .email(user.getEmail())
                .role(user.getGlobalRole().name())
                .fullName(user.getFullName())
                .build();

        return new AuthResult(tokenResponse, userResponse);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        // 1. Extract the username/email contained inside the stateless JWT claims
        String email = jwtService.extractUsername(refreshToken);

        if (email == null) {
            throw new RuntimeException("Invalid refresh token claims");
        }

        // 2. Fetch the user context from the database to ensure user still exists
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found from token context"));

        // 3. Cryptographically validate token integrity and expiration against user context
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 4. Issue a brand new access token and a brand new refresh token (Stateless rotation)
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void revokeRefreshToken(String refreshToken) {
        // Since tokens are stateless and no longer stored in a database allowlist,
        // you cannot manually revoke a single token before its natural expiration.
        // The Controller's maxAge(0) cookie logic will successfully clear it from the client's browser.
    }

    public void revokeAllUserTokens(String email) {
        // No-op step: Database records do not exist to be deleted.
    }

    public UserResponse getMe(String email) {
        var user = repository.findByEmail(email)
                .orElseThrow();

        return UserResponse.builder()
                .email(user.getEmail())
                .role(user.getGlobalRole().name())
                .fullName(user.getFullName())
                .build();
    }
}