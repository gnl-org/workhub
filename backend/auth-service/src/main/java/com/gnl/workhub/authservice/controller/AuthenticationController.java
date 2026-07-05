package com.gnl.workhub.authservice.controller;

import com.gnl.workhub.authservice.dto.*;
import com.gnl.workhub.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        TokenResponse tokenResponse = service.register(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(tokenResponse.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokenResponse.refreshToken()).toString())
                .body(new UserResponse(null, request.email(), "ROLE_USER", request.fullName()));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        try {
            AuthResult result = service.authenticate(request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(result.tokenResponse().accessToken()).toString())
                    .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(result.tokenResponse().refreshToken()).toString())
                    .body(result.userResponse());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Bad credentials"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token not found"));
        }
        try {
            TokenResponse newTokens = service.refreshAccessToken(refreshToken);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, createAccessTokenCookie(newTokens.accessToken()).toString())
                    .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(newTokens.refreshToken()).toString())
                    .body(Map.of("message", "Token refreshed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie("accessToken").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie("refreshToken").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie("XSRF-TOKEN").toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        var userDetails = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        return ResponseEntity.ok(service.getMe(userDetails.getUsername()));
    }

    private ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true).secure(false).path("/")
                .maxAge(15 * 60).sameSite("Lax").build();
    }

    private ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true).secure(false).path("/")
                .maxAge(24 * 60 * 60).sameSite("Lax").build();
    }

    private ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "").httpOnly(true).secure(false).path("/").maxAge(0).build();
    }
}
