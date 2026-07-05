package com.gnl.workhub.authservice.dto;

import java.util.UUID;

public record UserEvent(
        UUID id,
        String email,
        String fullName,
        String role,
        String eventType
) {}
