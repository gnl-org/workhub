package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private UUID assignedToId; // Can be null to unassign
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
}
