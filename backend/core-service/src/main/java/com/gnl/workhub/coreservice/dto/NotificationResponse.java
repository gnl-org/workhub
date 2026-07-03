package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.NotificationType;

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
