package com.gnl.workhub.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

        // 3.01
        UserResponse userResponse = UserResponse.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role("ROLE_USER")
                .build();

        // 3. Return successfully with the cookie inside the headers instead of the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(userResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        // 1. Service verifies credentials and generates the raw token
        AuthResult result = service.authenticate(request);
        String jwtToken = result.authResponse().getToken();
        UserResponse userResponse = result.userResponse();

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
                .body(userResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 1. Create the cookie that evicts the JWT token
        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false) // Set to true in production when running over HTTPS
                .path("/")
                .maxAge(0) // Instantly deletes the cookie in the user's browser
                .build();

        // 2. Create the cookie that evicts the CSRF token
        ResponseCookie csrfCookie = ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false) // Must match how it was created
                .secure(false)
                .path("/")
                .maxAge(0) // Instantly deletes the cookie in the user's browser
                .build();

        // 3. Return both Set-Cookie headers in the response
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString()) // Spring allows multiple SET_COOKIE headers
                .body(Map.of("message", "Logged out successfully")); // Fixed message alignment
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // 1. Safety check: If the security context is empty or unauthenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        // 2. Extract the UserDetails principal attached by your JwtAuthenticationFilter
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3 get user details
        UserResponse response = service.getMe(userDetails.getUsername());

        return ResponseEntity.ok(response);
    }
}