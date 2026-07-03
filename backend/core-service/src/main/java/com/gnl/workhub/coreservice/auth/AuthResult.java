package com.gnl.workhub.coreservice.auth;

public record AuthResult(TokenResponse tokenResponse, UserResponse userResponse) {}