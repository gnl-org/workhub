package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.TaskPriority;
import com.gnl.workhub.coreservice.enums.TaskStatus;
import com.gnl.workhub.coreservice.enums.TaskType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record TaskDetailsResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        TaskType taskType,
        Integer storyPoints,
        LocalDateTime dueDate,
        UUID projectId,
        String projectName,
        UserSummary assignee,
        UserSummary creator,
        List<TaskCommentDto.Response> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record UserSummary(UUID id, String fullName, String email) {}
}