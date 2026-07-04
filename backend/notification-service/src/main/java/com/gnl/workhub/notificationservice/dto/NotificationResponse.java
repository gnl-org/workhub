package com.gnl.workhub.notificationservice.dto;

import com.gnl.workhub.notificationservice.enums.NotificationType;

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
