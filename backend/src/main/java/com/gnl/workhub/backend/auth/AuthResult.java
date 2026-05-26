package com.gnl.workhub.backend.auth;

public record AuthResult(AuthenticationResponse authResponse, UserResponse userResponse) {}