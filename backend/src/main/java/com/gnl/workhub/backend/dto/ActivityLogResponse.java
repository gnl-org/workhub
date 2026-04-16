package com.gnl.workhub.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ActivityLogResponse {
    private UUID id;
    private String action;
    private String details;
    private String performedBy; // Full name of the user
    private String taskTitle;   // Title of the task affected
    private LocalDateTime timestamp;
}