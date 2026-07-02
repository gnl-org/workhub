package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String message,
        UUID taskId,
        UUID projectId,
        boolean isRead,
        LocalDateTime createdAt
) {}
