package com.gnl.workhub.coreservice.dto;

import com.gnl.workhub.coreservice.enums.TaskPriority;
import com.gnl.workhub.coreservice.enums.TaskStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskFilterRequest(
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeId,
        String search,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime start,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime end
) {}