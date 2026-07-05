package com.gnl.workhub.authservice.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String role,
        String fullName
) {}
