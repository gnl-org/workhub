package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {
    private NotificationType type;
    private UUID taskId;
    private UUID projectId;
    private UUID recipientId;
    private UUID triggeredByUserId;
    private String message;
}
