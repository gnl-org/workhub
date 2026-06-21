package com.gnl.workhub.backend.auth;

public record AuthResult(TokenResponse tokenResponse, UserResponse userResponse) {}