package com.gnl.workhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface TaskCommentDto {

    record Request(
            @NotBlank(message = "Comment content cannot be empty")
            String content
    ) implements TaskCommentDto {}

    record Response(
            UUID id,
            String content,
            String authorName,
            UUID authorId,
            LocalDateTime createdAt
    ) implements TaskCommentDto {}
}