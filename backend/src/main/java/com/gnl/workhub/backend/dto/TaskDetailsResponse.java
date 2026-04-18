package com.gnl.workhub.backend.dto;

import com.gnl.workhub.backend.enums.TaskPriority;
import com.gnl.workhub.backend.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public record TaskDetailsResponse(
        // Core Task Data
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,

        // Metadata/Context
        UUID projectId,
        String projectName,

        // People Involved
        UserSummary assignee,
        UserSummary creator,

        // Collaboration (The new part!)
        List<TaskCommentDto.Response> comments,

        // Timestamps
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // Nested helper for small user objects
    public record UserSummary(UUID id, String fullName, String email) {}
}