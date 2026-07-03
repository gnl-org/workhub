package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.TaskPriority;
import com.gnl.workhub.coreservice.enums.TaskStatus;
import com.gnl.workhub.coreservice.enums.TaskType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private UUID projectTitle;
    private String assigneeName;
    private TaskStatus status;
    private TaskPriority priority;
    private TaskType taskType;
    private Integer storyPoints;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID owner;
    private UUID workStageId;
    private UUID sprintId;
    private int sortOrder;
    private boolean inActiveSprint;
}