package com.gnl.workhub.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        // 1. Service still creates the user and signs the raw token
        AuthenticationResponse authResponse = service.register(request);
        String jwtToken = authResponse.getToken();

        // 2. Wrap the token in a secure HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
                .httpOnly(true)
                .secure(false) // Set to true when you redeploy to production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // Matches your JWT expiration (e.g., 24 hours in seconds)
                .sameSite("Lax")
                .build();

        // 3. Return successfully with the cookie inside the headers instead of the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Registration successful"));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        // 1. Service verifies credentials and generates the raw token
        AuthenticationResponse authResponse = service.authenticate(request);
        String jwtToken = authResponse.getToken();

        // 2. Wrap the token in a secure HttpOnly Cookie
        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
                .httpOnly(true)
                .secure(false) // Set to true for HTTPS/Prod. False for local dev
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        // 3. Keep the payload clear of sensitive strings
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Authentication successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0) // Instantly deletes the cookie in the user's browser
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Authentication successful"));
    }
}