package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;
import com.gnl.workhub.backend.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private UUID assignedToId;
    private TaskStatus status;
    private TaskPriority priority;
    private TaskType taskType;
    private Integer storyPoints;
    private LocalDateTime dueDate;
    private UUID workStageId;
}

