package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskRequest {
    private String title;
    private UUID projectId;    // Reference by ID
    private UUID assignedToId; // Reference by ID (can be null)
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
}