package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.TaskPriority;
import com.gnl.workhub.coreservice.enums.TaskStatus;
import com.gnl.workhub.coreservice.enums.TaskType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private UUID assignedToId;
    private TaskStatus status;
    private TaskPriority priority;
    private TaskType taskType;
    private Integer storyPoints;
    private LocalDateTime dueDate;
}
